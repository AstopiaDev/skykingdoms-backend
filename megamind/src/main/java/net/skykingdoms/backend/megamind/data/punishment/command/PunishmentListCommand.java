package net.skykingdoms.backend.megamind.data.punishment.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.command.exception.CommandException;
import net.skykingdoms.api.backend.punishment.PunishmentMeta;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.backend.base.data.punishment.PunishmentSqlHandler;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.base.util.paged.PagedList;
import net.skykingdoms.backend.base.util.string.StringUtil;
import net.skykingdoms.backend.base.util.time.Duration;
import net.skykingdoms.backend.base.util.time.TimeUtil;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.data.punishment.PunishmentManager;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author Vihara
 */
public abstract class PunishmentListCommand {

    protected void sendPunishmentsList(final @NonNull CoreExecutionSession session,
                                       final @NonNull PunishmentType type) throws CommandException {

        val gamer = session.getOfflineGamer(0, $gamer -> {
            $gamer.loadGroups();
            $gamer.loadPlayerName();
        });

        int page = 1;
        if (session.getArgumentCount() > 1) {
            page = session.getIntArgument(1, "page");
        }

        // retard fix
        if (page < 1) {
            page = 1;
        }

        val punishments = PunishmentManager.INSTANCE.getPunishments(gamer.getGamerId())
                .stream()
                .filter($type -> $type.getPunishmentType() == type)
                .collect(Collectors.toSet());

        // 10 mutes per page
        val punishmentMetas = punishments.stream().sorted((a1, a2) -> Integer.compareUnsigned(a1.getMetaId(), a2.getMetaId())).collect(PagedList.collector(10));

        // if page is more than overall amount of pages, we just set it to max value
        if (page > punishmentMetas.getPages()) {
            page = punishmentMetas.getPages();
        }

        // It's definitely better to use string builder instead of
        // sending all the shit in heap
        val sb = new StringBuilder();

        // Header
        //noinspection StringConcatenationInsideStringBufferAppend
        sb.append(ChatColor.YELLOW + "--- " + (StringUtil.capitalize(type.name().toLowerCase()) + "s") + " for ").append(gamer.getDisplayName()).append(ChatColor.YELLOW).append(" (Page ").append(page).append(" of ").append(punishmentMetas.getPages()).append(") ---\n");

        // appending each mute meta
        for (val meta : punishmentMetas.size() == 0 ? Collections.<PunishmentMeta>emptySet() : punishmentMetas.getPage(page - 1)) {
            @SuppressWarnings("DataFlowIssue") // we know executor gamer exists
            val executorString = meta.getExecutorId() == 0
                    ? "CONSOLE"
                    : Gamer.getOfflineGamer(meta.getExecutorId(), CommonGamer::loadGroupsAndName).getDisplayName();

            val executionString = TimeUtil.getDateTimeStringEu(meta.getExecutionDate());
            val durationString = Duration.format(meta.getExecutionDate(), meta.getExpirationTime(), null);
            val displayId = punishmentMetas.indexOf(meta) + 1;
            val typeString = meta.getPunishmentType() == PunishmentType.BAN
                    ? meta.isPermanent()
                        ? "PermaBan"
                        : "TempBan"
                    : "TempMute";

            sb.append(ChatColor.YELLOW).append(displayId).append(". ").append(typeString).append(" - ")
                    .append(executionString)
                    .append(" ").append(ChatColor.WHITE).append(ChatColor.ITALIC).append(meta.getReason()).append(ChatColor.RESET)
                    .append(" ").append(ChatColor.YELLOW).append("by ").append(executorString);

            if (!meta.isPermanent()) {
                sb.append(" ").append(ChatColor.YELLOW).append("for ").append(durationString);
            }

            val removeMeta = PunishmentSqlHandler.INSTANCE.getPunishmentRemoveMeta(meta.getLiftMeta()).join();
            if (removeMeta != null) {
                val removerString = removeMeta.getRemoverId() < 1
                        ? "CONSOLE"
                        : Gamer.getOfflineGamer(removeMeta.getRemoverId(), CommonGamer::loadGroupsAndName).getDisplayName();
                val removedString = TimeUtil.getDateTimeStringEu(removeMeta.getDate());
                val actionString = type == PunishmentType.BAN ? "unbanned" : "unmuted";

                sb.append(ChatColor.YELLOW).append(" ").append(actionString).append(" ").append(removedString)
                        .append(" ").append(ChatColor.WHITE).append(ChatColor.ITALIC).append(removeMeta.getReason()).append(ChatColor.RESET)
                        .append(" ").append(ChatColor.YELLOW).append("by ").append(removerString);
            }

            sb.append("\n");
        }

        session.sendMessage(sb.toString());
    }

}
