package net.skykingdoms.minimind.social.listener;

import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.play.PostEventPacket;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayProcessor;
import net.skykingdoms.minimind.social.SocialMind;

public class CoreListener implements PlayProcessor {
    public CoreListener() {
        SocialMind.getInstance().getConnection().getChannel().registerProcessor(this);
    }

    @Override
    public void process(PostEventPacket packet, ChannelProcessorContext ctx) {
        SocialMind.getInstance().getEventBus().post(packet.getEvent());
    }
}
