package net.skykingdoms.backend.protocol.pipeline;

import io.netty.channel.socket.SocketChannel;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.protocol.channel.AbstractChannel;

public enum Pipeline {
    INSTANCE;

    private final String FRAMER = "packet-framer"; // cocker
    private final String ENCODER = "packet-encoder";
    private final String DECODER = "packet-decoder";
    private final String HANDLER = "packet-handler";
    private final String IN_LOG = "packet-inbound-logger";
    private final String OUT_LOG = "packet-outbound-logger";

    public void initPipeline(final @NonNull AbstractChannel channel,
                             final @NonNull SocketChannel socket) {
        val pipeline = socket.pipeline();

        pipeline.addLast(FRAMER, new PacketFramer());
        pipeline.addLast(ENCODER, new PacketEncoder(channel.getOutboundPacketDirection()));
        pipeline.addLast(DECODER, new PacketDecoder(channel.getInboundPacketDirection()));
        pipeline.addLast(IN_LOG, new PacketInboundLogger());
        pipeline.addLast(OUT_LOG, new PacketOutboundLogger());
        pipeline.addLast(HANDLER, new PacketHandler(channel.newPacketProcessor(socket)));
    }
}
