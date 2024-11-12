package net.skykingdoms.backend.megamind.data.punishment;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.punishment.PunishmentMeta;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.megamind.event.impl.core.PunishmentLiftEvent;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.data.punishment.PunishmentSqlHandler;
import net.skykingdoms.backend.base.dependency.Dependency;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.base.util.time.Duration;
import net.skykingdoms.backend.base.util.time.DurationField;
import net.skykingdoms.backend.base.util.time.DurationFormatter;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.manager.ProxyManager;
import net.skykingdoms.backend.megamind.manager.StaffChatManager;
import net.skykingdoms.backend.protocol.packet.play.S24PlayerMuteAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Primary punishments manager
 * @author Vihara
 */
@Getter
public final class PunishmentManager {

    // ----------------------------------------------------------------------
    @Dependency
    public static final PunishmentManager INSTANCE = new PunishmentManager();
    // ----------------------------------------------------------------------

    /**
     * List of all active punishments
     */
    Collection<PunishmentMeta> activePunishments = CollectionUtil.createConcurrentSet();

    /**
     * Invoked once. Loads all punishments from database
     */
    public void postBootstrap() {
        val epoch = System.currentTimeMillis();

        PunishmentSqlHandler.INSTANCE.loadAllPunishments().whenComplete((punishmentMetas, throwable) -> {

            // adding only lifted punishments
            activePunishments.addAll(punishmentMetas.stream().filter(c -> !c.isLifted()).collect(Collectors.toSet()));

            Logging.IMP.info("[PunishmentManager] Loaded {} active punishments ({} ms)",
                    activePunishments.size(), System.currentTimeMillis() - epoch);
        }).join();

        // ticking all punishments every second
        AsyncExecution.INSTANCE.runTimer(this::tickPunishments, 0, 1, TimeUnit.SECONDS);
    }

    public PunishmentMeta getById(final int metaId) {
        return activePunishments.stream()
                .filter(meta -> meta.getMetaId() == metaId)
                .findFirst()
                // getting from database, if no active shiet loaded
                .orElse(PunishmentSqlHandler.INSTANCE.getPunishment(metaId).join());
    }

    /**
     * Player may have only one active punishment per each punishment type
     */
    public PunishmentMeta getActivePunishment(final int gamerId,
                                              final @NonNull PunishmentType punishmentType) {
        return activePunishments.stream()
                .filter(meta -> meta.getPunishmentType() == punishmentType)
                .filter(meta -> !meta.isLifted() && meta.getViolatorId() == gamerId && !meta.isExpired())
                .findFirst()
                .orElse(null);
    }

    public Collection<PunishmentMeta> getPunishments(final int gamerId) {
        // we don't cache this shit because calling it not frequently
        // todo: cache, if high loads
        return PunishmentSqlHandler.INSTANCE.loadAllPunishments(gamerId).join();
    }

    public void sendBungeeMutePacket(final PunishmentMeta meta) {
        if (meta.getPunishmentType() != PunishmentType.MUTE) {
            throw new IllegalArgumentException("Dolbaeb blya not a mute punishment!");
        }

        ProxyManager.INSTANCE.sendToProxies(new S24PlayerMuteAction(
                S24PlayerMuteAction.Action.ADD_MUTE,
                meta.getMetaId(),
                meta.getViolatorId(),
                meta.getReason(),
                meta.getExpirationTime()));
    }

    public void removePunishment(final int metaId,
                                 final int remover,
                                 final @NonNull String reason) {
        val meta = getById(metaId);

        // isLifted() actually may return false if it is expired
        // for example, if ban expired while coordinator was down :/
        if (meta == null) {
            return; // the punishment doesnt exist
        }

        // marking punishment as lifted
        // after next coordinator reboot
        // it won't load to collection
        meta.setLifted(true);

        // removing shit
        activePunishments.remove(meta);

        val executorString = remover < 1 ? ChatColor.WHITE + "CONSOLE" : Gamer.getOfflineGamer(remover, CommonGamer::loadGroupsAndName).getDisplayName();

        val liftMeta = PunishmentSqlHandler.INSTANCE.createPunishmentLiftRecord(remover, reason);

        val gamer = Gamer.getOfflineGamer(meta.getViolatorId(), CommonGamer::loadGroupsAndName);
        if (meta.getPunishmentType() == PunishmentType.MUTE) {
            ProxyManager.INSTANCE.sendToProxies(new S24PlayerMuteAction(
                    S24PlayerMuteAction.Action.REMOVE_MUTE,
                    metaId,
                    -1, null, null // any value. they are not going to be written to packet
            ));

            if (gamer != null && gamer.isOnline()) {
                // notifying a player that his mute expired
                gamer.sendLocalizedMessage("punishment_mute_lifted");
            }

            StaffChatManager.INSTANCE.sendMessage("staffchat_unmuted_message",
                    "%executor%", executorString,
                    "%violator%", gamer.getDisplayName(),
                    "%reason%", reason);
        } else {
            StaffChatManager.INSTANCE.sendMessage("staffchat_unbanned_message",
                    "%executor%", executorString,
                    "%violator%", gamer.getDisplayName(),
                    "%reason%", reason);
        }

        PunishmentSqlHandler.INSTANCE.updatePunishment(metaId, meta.getExpirationTime(), true, liftMeta.getMetaId());

        MegamindApp.INSTANCE.getPluginManager().callEvent(new PunishmentLiftEvent(meta, liftMeta));
    }

    public PunishmentMeta performPunishment(final int gamerId,
                                  final int executorId,
                                  final @NonNull String reason,
                                  final @Nullable java.time.Duration duration,
                                  final @NotNull String originatedServer,
                                  final @NonNull PunishmentType type) {
        // already punished
        if (getActivePunishment(gamerId, type) != null) {
            return null;
        }

        val bakedPunishmentMeta = PunishmentSqlHandler.INSTANCE.createPunishmentRecord(
                gamerId,
                executorId, reason, duration == null ? null : LocalDateTime.now().plus(duration),
                originatedServer,
                type);

        activePunishments.add(bakedPunishmentMeta);

        val gamer = Gamer.getOfflineGamer(gamerId, CommonGamer::loadGroups);

        // if gamer is online we disconnect or send a "muted" message
        if (gamer != null && gamer.isOnline()) {
            if (gamer.getCurrentServer() != null) {
                Arrays.stream(gamer.getCurrentServer().getPlayerIds()).mapToObj(Gamer::getGamer).forEach(iGamer -> {
                    iGamer.sendLocalizedMessage(bakedPunishmentMeta.isWatchCatBan() ? "watchcat_ban_broadcast" : "staff_ban_broadcast");
                });
            }

            if (type == PunishmentType.BAN) {
                gamer.disconnect(getPunishmentReasonFormatted(bakedPunishmentMeta, Language.DEFAULT));
            } else {
                gamer.sendMessage(getPunishmentReasonFormatted(bakedPunishmentMeta, (Language) gamer.getLanguage()));
            }
        }

        if (type == PunishmentType.MUTE) {
            sendBungeeMutePacket(bakedPunishmentMeta);
        }

        val durationString = bakedPunishmentMeta.isPermanent() ? null : Duration.format(duration.toMillis(), Duration.FormatParams.builder()
                .sourceUnit(TimeUnit.MILLISECONDS)
                .locale(DurationFormatter.EN_SHORT)
                .min(DurationField.SEC)
                .max(DurationField.DAY).build());
        val executorString = executorId == 0 ? ChatColor.WHITE + "CONSOLE" : Gamer.getOfflineGamer(executorId, CommonGamer::loadGroupsAndName).getDisplayName();

        // don't look at warnings, we know what we do :3
        if (type == PunishmentType.BAN && !bakedPunishmentMeta.isWatchCatBan()) {
            if (bakedPunishmentMeta.isPermanent()) {
                StaffChatManager.INSTANCE.sendMessage("staffchat_permabanned_message",
                        "%executor%", executorString,
                        "%violator%", gamer.getDisplayName(),
                        "%reason%", reason);
            } else {
                StaffChatManager.INSTANCE.sendMessage("staffchat_tempbanned_message",
                        "%executor%", executorString,
                        "%violator%", gamer.getDisplayName(),
                        "%reason%", reason,
                        "%duration%", durationString);
            }
        } else if (type == PunishmentType.MUTE) {
            StaffChatManager.INSTANCE.sendMessage("staffchat_muted_message",
                    "%executor%", executorString,
                    "%violator%", gamer.getDisplayName(),
                    "%reason%", reason,
                    "%duration%", durationString);
        }

        return bakedPunishmentMeta;
    }

    public @NotNull String getPunishmentReasonFormatted(final @NonNull PunishmentMeta meta,
                                                        final @NonNull Language language) {
        val sb = new StringBuilder();

        val formatter = Duration.FormatParams.builder()
                .sourceUnit(TimeUnit.MILLISECONDS)
                .min(DurationField.SEC)
                .max(DurationField.DAY)
                // todo: support multiple locales, not only two
                .locale(language == Language.RUSSIAN ? DurationFormatter.RU_SHORT : DurationFormatter.EN_SHORT)
                .build();

        // since now to expiration
        val durationString = Duration.format(LocalDateTime.now(), meta.getExpirationTime(), formatter);

        // we do not translate BAN message
        if (meta.getPunishmentType() == PunishmentType.BAN) {
            sb.append(ChatColor.RED + "You are ").append(meta.isPermanent() ? "permanently" : "temporary").append(" banned");
            if (!meta.isPermanent()) {
                sb.append(" for ").append(ChatColor.WHITE)
                        .append(durationString);
            }

            sb.append(ChatColor.RED).append(" from this server!\n\n");
            sb.append(ChatColor.GRAY).append("Reason: ").append(ChatColor.WHITE).append(meta.getReason()).append("\n");
            sb.append(ChatColor.GRAY).append("Find out more: ").append(ChatColor.AQUA).append(ChatColor.UNDERLINE).append("https://skykingdoms.net/\n\n");
            sb.append(ChatColor.GRAY).append("Ban ID: ").append(ChatColor.WHITE).append("#").append(Integer.toHexString(meta.getMetaId())).append("\n");
            sb.append(ChatColor.GRAY).append("Sharing your Ban ID may affect the processing of your appeal!");
        } else { // but translate mute messages
            sb.append(language.getMessage(false, "punishment_muted",
                    "%reason%", meta.getReason(),
                    "%duration%", durationString,
                    "%muteid%", meta.getMetaId()));
        }

        return sb.toString();
    }

    public void tickPunishments() {
        val pendingRemoval = new HashSet<PunishmentMeta>();

        for (val punishment : activePunishments) {
            if (!punishment.isExpired()) {
                continue;
            }

            Logging.IMP.info("[PunishmentManager] {} #{} expired",
                    punishment.getPunishmentType(), punishment.getMetaId());

            pendingRemoval.add(punishment);
        }

        // $ CME Connoisseur $
        pendingRemoval.forEach(meta -> removePunishment(meta.getMetaId(), -1, meta.getPunishmentType() == PunishmentType.BAN
                ? "Tempban expired"
                : "Mute expired"));
    }

}
