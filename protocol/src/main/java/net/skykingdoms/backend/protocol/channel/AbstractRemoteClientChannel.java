package net.skykingdoms.backend.protocol.channel;

import io.netty.channel.socket.SocketChannel;
import lombok.NonNull;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.handshake.processor.HandshakeClientProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;

public abstract class AbstractRemoteClientChannel extends AbstractRemoteChannel
        implements HandshakeClientProcessor, PlayClientProcessor {

    public AbstractRemoteClientChannel(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void process(@NonNull ChannelProcessorContext ctx) throws Exception {
        ctx.callProcess(this);
        handlePacket(ctx);
    }
}
