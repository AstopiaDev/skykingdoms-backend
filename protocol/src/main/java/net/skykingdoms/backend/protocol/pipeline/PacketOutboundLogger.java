package net.skykingdoms.backend.protocol.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.NonNull;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.Packet;

public final class PacketOutboundLogger extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(final @NonNull ChannelHandlerContext ctx,
                      final @NonNull Object msg,
                      final @NonNull ChannelPromise promise) throws Exception {
        if (msg instanceof Packet<?>) {
            Logging.IMP.debug("[{}] Outbound packet: {}", ctx.channel().remoteAddress(), msg);
        }

        super.write(ctx, msg, promise);
    }
}
