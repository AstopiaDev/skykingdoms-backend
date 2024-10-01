package net.skykingdoms.minimind.social.party;

import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerQuitEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.PlayerGameRedirectEvent;
import net.skykingdoms.minimind.social.SocialGamer;

/**
 * @author Vihara
 */
public final class PartyListener implements Listener {
    @EventHandler
    public void onQuit(final GamerQuitEvent event) {
        PartyManager.INSTANCE.handleDisconnect((SocialGamer) event.getGamer());
    }

    @EventHandler
    public void onJoin(final GamerJoinEvent event) {
        SocialGamer gamer = (SocialGamer) event.getGamer();
        gamer.setParty(PartyManager.INSTANCE.getParty(gamer.getGamerId()));
        PartyManager.INSTANCE.handleJoin((SocialGamer) event.getGamer());
    }

    @EventHandler
    public void onRedirect(PlayerGameRedirectEvent event) {
        SocialGamer gamer = (SocialGamer) event.getGamer();
        Party party = gamer.getParty();

        if (party != null && party.isLeader(gamer)) {
            event.getAdditionalGamerList().addAll(party.getAllMembers());
        }
    }
}
