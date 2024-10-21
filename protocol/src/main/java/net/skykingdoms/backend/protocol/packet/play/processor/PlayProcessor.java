package net.skykingdoms.backend.protocol.packet.play.processor;

import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.play.*;


public interface PlayProcessor extends PacketProcessor {
    default void process(PostEventPacket packet, ChannelProcessorContext ctx) {
        //override me
    }

    default void process(B101UpdateRank packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(B109UpdateSetting packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(B110UserUpdatePermission packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(B111GroupUpdatePermission packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(B107ProxyKick packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(B108UpdatePlayerLanguage packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(B102SetProperty packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(B103RemoveProperty packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(B106Chat packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(B104BroadcastChat packet, ChannelProcessorContext ctx) {
        // override me
    }
}
