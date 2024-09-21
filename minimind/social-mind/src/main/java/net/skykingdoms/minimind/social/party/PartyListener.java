package net.skykingdoms.minimind.social.party;

import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import net.skykingdoms.minimind.social.SocialGamer;
import net.skykingdoms.minimind.social.SocialMind;

/**
 * @author Vihara
 */
public final class PartyListener implements PlayServerProcessor {

    public PartyListener() {
        SocialMind.getInstance().getConnection().getChannel().registerProcessor(this);
    }

/*    @Evve
    public void process(final GamerQuitEventPacket packet, ChannelProcessorContext ctx) {
        SocialGamer gamer = (SocialGamer) SocialGamer.getGamer(packet.getGamerId());
        gamer.setOnline(false);
        PartyManager.INSTANCE.handleDisconnect(gamer);
    }

    @Override
    public void process(final GamerJoinEventPacket packet, ChannelProcessorContext ctx) {
        SocialGamer gamer = (SocialGamer) SocialGamer.getGamer(packet.getGamerId());
        gamer.setOnline(true);
        gamer.setCurrentServer(packet.getServer());
        gamer.setParty(PartyManager.INSTANCE.getParty(gamer.getGamerId()));
        Logging.IMP.info("{}, {}", gamer.isOnline(), gamer.getCurrentServer());
        PartyManager.INSTANCE.handleJoin(gamer);
    }*/

/*    @EventHandler
    public void onPlayerRedirect(PlayerGameRedirectEvent event) {
        Party party = event.getGamer().getParty();

        if (party != null && party.isLeader(event.getGamer())) {
            event.getAdditionalGamerList().addAll(party.getAllMembers());
        }
    }*/
}
