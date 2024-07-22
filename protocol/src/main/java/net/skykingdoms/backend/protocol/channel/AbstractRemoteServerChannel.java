package net.skykingdoms.backend.protocol.channel;

import io.netty.channel.socket.SocketChannel;
import lombok.NonNull;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.handshake.processor.HandshakeServerProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;

public abstract class AbstractRemoteServerChannel extends AbstractRemoteChannel
        implements HandshakeServerProcessor, PlayServerProcessor {

    private final AbstractClientChannel client;

    public AbstractRemoteServerChannel(AbstractClientChannel client, SocketChannel channel) {
        super(channel);

        this.client = client;
    }

    @Override
    public void process(@NonNull ChannelProcessorContext ctx) throws Exception {
        ctx.callProcess(this);
        handlePacket(ctx);
    }

    @Override
    protected final void onDisconnect() {
        if (!client.isReconnecting()) {
            onServerDisconnect();
        }

        client.reconnect();
    }

    protected void onServerDisconnect() {

    }
}
