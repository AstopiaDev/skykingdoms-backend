package net.skykingdoms.backend.protocol.channel;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.socket.SocketChannel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.backend.protocol.packet.BossProcessor;
import net.skykingdoms.backend.protocol.packet.PacketDirection;
import net.skykingdoms.backend.protocol.pipeline.Pipeline;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@RequiredArgsConstructor
public abstract class AbstractChannel {

    protected static final boolean epoll = Epoll.isAvailable();

    protected static final ThreadFactory threadFactory = new ThreadFactory() {

        private final AtomicInteger counter = new AtomicInteger();

        @Contract("_ -> new")
        @Override
        public @NotNull Thread newThread(final @NonNull Runnable r) {
            return new Thread(r, "[Netty] EventLoopGroup #" + counter.incrementAndGet());
        }
    };

    protected ChannelInitializer<SocketChannel> channelInitializer;

    @Getter
    protected final SocketAddress socketAddress;
    protected final int threads;

    public abstract BossProcessor newPacketProcessor(SocketChannel channel);

    public abstract PacketDirection getOutboundPacketDirection();

    public abstract PacketDirection getInboundPacketDirection();

    public boolean isEpoll() {
        return epoll;
    }

    protected void initPipeline(final @NonNull SocketChannel channel) {
        Pipeline.INSTANCE.initPipeline(this, channel);
    }

    protected void initChannelInitializer() {
        channelInitializer = new ChannelInitializer<>() {
            @Override
            protected void initChannel(final @NonNull SocketChannel ch) {
                initPipeline(ch);
            }
        };
    }

    protected void init() {
        initChannelInitializer();
    }

}
