package net.skykingdoms.backend.megamind.data.punishment;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerProxyLoginEvent;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.event.core.ClientActiveEvent;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;

/**
 * @author Vihara
 */
public final class PunishmentListener implements Listener, PlayClientProcessor {

    public PunishmentListener() {
        MegamindApp.INSTANCE.pluginManager().registerListener(this);
    }

    @EventHandler
    public void onBanLogin(final @NonNull GamerProxyLoginEvent event) {
        val activePunishment = PunishmentManager.INSTANCE.getActivePunishment(((Gamer) event.getGamer()).getGamerId(), PunishmentType.BAN);
        if (activePunishment != null) {
            event.setCancelled(true);
            // we format ban by default language
            event.setKickReason(PunishmentManager.INSTANCE.getPunishmentReasonFormatted(activePunishment, Language.DEFAULT));
        }
    }

    @EventHandler
    public void onMutedPlayerJoin(final GamerJoinEvent event) {
        val player = (Gamer) event.getGamer();

        val muteMeta = PunishmentManager.INSTANCE.getActivePunishment(player.getGamerId(), PunishmentType.MUTE);

        if (muteMeta != null) {
            // sending a packet to bungee to notify we got a here
            PunishmentManager.INSTANCE.sendBungeeMutePacket(muteMeta);
        }
    }

    @EventHandler
    public void onActive(ClientActiveEvent event) {
        event.getProcessor().registerProcessor(this);
    }

}
