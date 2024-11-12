package net.skykingdoms.backend.protocol.channel;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.internal.SystemPropertyUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.BossProcessor;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProtocol;
import net.skykingdoms.backend.protocol.pipeline.PacketDecoder;
import net.skykingdoms.backend.protocol.pipeline.PacketEncoder;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"rawtypes", "unchecked"})
@RequiredArgsConstructor
public abstract class AbstractRemoteChannel extends BossProcessor {

    public static final long DEFAULT_TIMEOUT = SystemPropertyUtil.getInt("requestTimeout", 5000);

    protected final SocketChannel channel;
    protected final AtomicInteger requestIdCounter = new AtomicInteger();

    protected final ConcurrentMap<Integer, CompletableFuture> responseHandlers = new ConcurrentHashMap<>();

    private final ExecutorService packetProcessor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor();

    @Getter
    private PacketProtocol protocol;

    protected int nextRequestId() {
        int requestId = requestIdCounter.incrementAndGet();
        if (requestId == Integer.MAX_VALUE) {
            requestIdCounter.set(0);
        }
        return requestId;
    }

    public void upgradeConnection(PacketProtocol protocol) {
        if (isActive()) {
            channel.pipeline().get(PacketEncoder.class).upgradeConnection(protocol);
            channel.pipeline().get(PacketDecoder.class).upgradeConnection(protocol);
            this.protocol = protocol;
        }
    }

    protected void addResponseHandler(int requestId, @NonNull CompletableFuture handler) {
        responseHandlers.put(requestId, handler);
    }

    protected CompletableFuture<Packet.Response> removeResponseHandler(int requestId) {
        return responseHandlers.remove(requestId);
    }

    protected void handlePacket(@NonNull ChannelProcessorContext ctx) {
        Packet<?> packet = ctx.getPacket();

        if (packet instanceof Packet.Response) {
            Packet.Response<?> response = (Packet.Response<?>) packet;
            CompletableFuture<Packet.Response> handler = removeResponseHandler(response.getRequestId());

            if (handler == null) {
                return;
            }

            handler.complete(response);
        }
    }

    public boolean isActive() {
        return channel.isActive();
    }

    public void close() {
        if (!isActive()) {
            return;
        }
        channel.close();
    }

    public void sendPacket(@NonNull Packet<?> packet) {
        if (!isActive()) {
            return;
        }

        channel.writeAndFlush(packet, channel.voidPromise());
    }

    public void sendPacket(@NonNull Packet<?> packet, Runnable onComplete) {
        if (!isActive()) {
            return;
        }

        packetProcessor.submit(() -> {
            channel.writeAndFlush(packet, channel.voidPromise());
        });
    }

    public <A extends Packet.Response<?>, B extends Packet.Request<?, A>> CompletableFuture<A> awaitPacket(@NonNull B packet) {
        return awaitPacket(packet, DEFAULT_TIMEOUT);
    }

    public <A extends Packet.Response<?>, B extends Packet.Request<?, A>> CompletableFuture<A> awaitPacket(@NonNull B packet, long timeout) {
        if (!isActive()) {
            throw new IllegalStateException("Channel is closed");
        }

        CompletableFuture<A> response = new CompletableFuture<>();
        int requestId = nextRequestId();

        addResponseHandler(requestId, response);
        packet.setRequestId(requestId);
        sendPacket(packet);

        timeoutScheduler.schedule(() -> {
            CompletableFuture<?> oldHandler = removeResponseHandler(requestId);
            if (oldHandler != null) {
                oldHandler.completeExceptionally(ReadTimeoutException.INSTANCE);
            }
        }, timeout, TimeUnit.MILLISECONDS);

        return response;
    }

    @Override
    public final void inactive() {
        protocol = PacketProtocol.HANDSHAKE;

        if (!responseHandlers.isEmpty()) {
            ClosedChannelException exception = new ClosedChannelException();
            responseHandlers.forEach((id, handler) -> handler.completeExceptionally(exception));
            responseHandlers.clear();
        }

        onDisconnect();
    }

    protected void onDisconnect() {
        // Override to handle specific disconnection actions
    }
}
