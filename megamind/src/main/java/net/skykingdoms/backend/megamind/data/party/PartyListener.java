package net.skykingdoms.backend.megamind.data.party;

import net.skykingdoms.backend.megamind.api.event.EventHandler;
import net.skykingdoms.backend.megamind.api.plugin.Listener;
import net.skykingdoms.backend.megamind.event.gamer.GamerJoinEvent;
import net.skykingdoms.backend.megamind.event.gamer.GamerLoadEvent;
import net.skykingdoms.backend.megamind.event.gamer.GamerQuitEvent;
import net.skykingdoms.backend.megamind.event.gamer.PlayerGameRedirectEvent;
import net.skykingdoms.backend.megamind.MegamindApp;

/**
 * @author Vihara
 */
public final class PartyListener implements Listener {

    public PartyListener() {
        MegamindApp.INSTANCE.pluginManager().registerListener(this);
    }

    @EventHandler
    public void onPlayerDisconnect(final GamerQuitEvent event) {
        PartyManager.INSTANCE.handleDisconnect(event.getGamer());
    }

    @EventHandler
    public void onPlayerJoin(final GamerJoinEvent event) {
        PartyManager.INSTANCE.handleJoin(event.getGamer());
    }

    @EventHandler
    public void onLoad(final GamerLoadEvent event) {
        event.getGamer().setParty(PartyManager.INSTANCE.getParty(event.getGamer().getGamerId()));
    }

    @EventHandler
    public void onPlayerRedirect(PlayerGameRedirectEvent event) {
        Party party = event.getGamer().getParty();

        if (party != null && party.isLeader(event.getGamer())) {
            event.getAdditionalGamerList().addAll(party.getAllMembers());
        }
    }
}
