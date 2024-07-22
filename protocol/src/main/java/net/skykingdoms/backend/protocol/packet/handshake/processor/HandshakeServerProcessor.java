package net.skykingdoms.backend.protocol.packet.handshake.processor;

import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;

/**
 * Processes packets FROM server
 *
 * 
 */
public interface HandshakeServerProcessor extends HandshakeProcessor {

    default void process(Handshake.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

}
