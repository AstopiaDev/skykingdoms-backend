package net.skykingdoms.backend.velocity.punishments;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.velocity.gamer.Gamer;

import java.util.Collection;

/**
 * A quick and basic realization of mutes handling on bungee side
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class VelocityPunishmentManager {

    // ----------------------------------------------------------------------------------
    public static final VelocityPunishmentManager INSTANCE = new VelocityPunishmentManager();
    // ----------------------------------------------------------------------------------

    Collection<MinimalMuteData> activeMutes = CollectionUtil.createConcurrentSet();

    public void addMute(final @NonNull MinimalMuteData minimalMuteData) {
        if (activeMutes.contains(minimalMuteData)) {
            return;
        }

        activeMutes.add(minimalMuteData);
    }

    public void removeMute(final int muteId) {
        activeMutes.removeIf(m -> m.getMuteId() == muteId);
    }

    public boolean isMuted(final int gamerId) {
        return activeMutes.stream().anyMatch(meta -> meta.getViolatorId() == gamerId && !meta.isExpired());
    }

    public String getMuteMessage(final @NonNull Gamer gamer) {
        val muteMeta = activeMutes.stream()
                .filter(m -> m.getViolatorId() == gamer.getGamerId())
                .findFirst().orElseThrow();

        return gamer.getLanguage().getMessage(gamer.isModern(), "punishment_muted",
                "%reason%", muteMeta.getReason(),
                "%duration%", muteMeta.getExpirationFormatted((Language) gamer.getLanguage()),
                "%muteid%", muteMeta.getMuteId());
    }

    /**
     * Called on gamer reconnect.
     * Basically, if mute was removed on core while it was disconnected
     * we won't get notified and mute will still be active.
     */
    public void invalidateMetas(final @NonNull Gamer gamer) {
        activeMutes.removeIf(minimalMuteData -> minimalMuteData.getViolatorId() == gamer.getGamerId());
    }
}
