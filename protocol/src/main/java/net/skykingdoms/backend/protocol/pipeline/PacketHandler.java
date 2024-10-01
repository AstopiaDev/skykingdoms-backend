package net.skykingdoms.backend.protocol.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.BossProcessor;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class PacketHandler extends SimpleChannelInboundHandler<Packet<?>> {

    private final BossProcessor processor;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        processor.callActive();
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        processor.callInactive();
    }

    @Override
    public void channelRead0(@NotNull ChannelHandlerContext ctx, @NotNull Packet packet) {
        ChannelProcessorContext context = new ChannelProcessorContext(packet, processor, ctx);

        try {
            processor.callProcessor(context);
        } catch (Throwable t) {
            Logging.IMP.stacktrace(t, "Error occurred whilst processing packet: " + packet);
        } finally {
            context.post();
        }
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) throws Exception {
        processor.callException(cause);
    }

}