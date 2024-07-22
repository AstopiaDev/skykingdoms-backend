package net.skykingdoms.backend.megamind.connection;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.internal.SystemPropertyUtil;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteClientChannel;
import net.skykingdoms.backend.protocol.channel.AbstractServerChannel;

import java.net.SocketAddress;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class LocalServerConnection extends AbstractServerChannel {

    static int LOW_MARK = SystemPropertyUtil.getInt("net.astrona.internal.low_mark", 2 << 18); // 0.5 mb
    static int HIGH_MARK = SystemPropertyUtil.getInt("net.astrona.internal.high_mark", 2 << 20); // 2 mb
    static WriteBufferWaterMark MARK = new WriteBufferWaterMark(LOW_MARK, HIGH_MARK);

    public LocalServerConnection(final @NonNull SocketAddress address, final int threads) {
        super(address, threads);
    }

    @Override
    protected AbstractRemoteClientChannel newClientChannel(SocketChannel channel) {
        return new RemoteClientConnection(channel);
    }

    @Override
    protected void initPipeline(final @NonNull SocketChannel channel) {
        super.initPipeline(channel);
        try {
            channel.config().setOption(ChannelOption.IP_TOS, 0x18);
        } catch (Exception ignored) {
        }
        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
        channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);
        channel.config().setWriteBufferWaterMark(MARK);
    }

}
