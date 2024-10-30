package net.skykingdoms.backend.protocol.packet;

import io.netty.channel.ChannelHandlerContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.api.protocol.logging.Logging;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ChannelProcessorContext {

    Packet packet;

    // first processor defined in PacketHandler
    @Getter
    BossProcessor mainProcessor;

    @Getter
    ChannelHandlerContext context;

    AtomicInteger latch = new AtomicInteger();
    AtomicBoolean fired = new AtomicBoolean();

    @Getter
    @NonFinal Packet<?> response;

    public ChannelProcessorContext(final @NonNull Packet<?> packet,
                                   final @NonNull BossProcessor mainProcessor,
                                   final @NonNull ChannelHandlerContext context) {
        this.packet = packet;
        this.mainProcessor = mainProcessor;
        this.context = context;
    }

    public void addLatch() {
        latch.incrementAndGet();
    }

    public void removeLatch() {
        if (latch.decrementAndGet() == 0 && fired.get()) {
            done();
        }
    }

    @SuppressWarnings("unchecked")
    public void callProcess(PacketProcessor processor) throws Exception {
        packet.process(processor, this);
    }

    public Packet<?> getPacket() {
        return packet;
    }

    /**
     * Set response
     * <p>
     * If not a request received, but response sent....retardation
     * to avoid this use {@link #setStrictResponse}
     */
    public void setResponse(final @NonNull Packet.Response<?> response) {
        this.response = response;

        if (!(packet instanceof Packet.Request<?, ?>)) return;

        Packet.Request<?, ?> request = (Packet.Request<?, ?>) packet;

        if (!request.hasRequestId()) return;

        response.setRequestId(request.getRequestId());
    }

    /**
     * Set response to request 0_o
     * <p>
     * if not request received response won't be sent
     * to avoid this use {@link #setResponse}
     */
    public void setStrictResponse(@NonNull Supplier<Packet.Response<?>> supplier) {
        if (!(packet instanceof Packet.Request<?, ?>)) return;

        Packet.Request<?, ?> request = (Packet.Request<?, ?>) packet;

        if (!request.hasRequestId()) return;

        setResponse(supplier.get());
    }

    public void done() {
        if (response != null) {
            context.writeAndFlush(response);
        }
    }

    public void post() {
        if (latch.get() == 0) {
            done();
        }

        fired.set(true);
    }


}
