package net.skykingdoms.backend.protocol.packet.handshake.processor;

import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;

/**
 * Processes packet FROM client
 *
 * 
 */
public interface HandshakeClientProcessor extends HandshakeProcessor {

    default void process(Handshake.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

}
