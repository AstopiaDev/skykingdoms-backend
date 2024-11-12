package net.skykingdoms.backend.protocol.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.BossProcessor;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public final class PacketHandler extends SimpleChannelInboundHandler<Packet<?>> {

    private final BossProcessor processor;
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        processor.callActive();
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        processor.callInactive();
        executorService.shutdown();
    }

    @Override
    public void channelRead0(@NotNull ChannelHandlerContext ctx, @NotNull Packet packet) {
        if (packet instanceof Handshake.Response || packet instanceof Handshake.Request) {
            ChannelProcessorContext context = new ChannelProcessorContext(packet, processor, ctx);

            try {
                processor.callProcessor(context);
            } catch (Throwable t) {
                Logging.IMP.stacktrace(t, "Error occurred whilst processing packet: " + packet);
            } finally {
                context.post();
            }
            return;
        }

        executorService.submit(() -> {
            ChannelProcessorContext context = new ChannelProcessorContext(packet, processor, ctx);

            try {
                processor.callProcessor(context);
            } catch (Throwable t) {
                Logging.IMP.stacktrace(t, "Error occurred whilst processing packet: " + packet);
            } finally {
                context.post();
            }
        });
    }

    @Override
    public void exceptionCaught(@NotNull ChannelHandlerContext ctx, @NotNull Throwable cause) throws Exception {
        processor.callException(cause);
    }
}
