package net.skykingdoms.backend.node.connection;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;

import java.util.concurrent.CompletableFuture;

@UtilityClass
public final class NodeConnection {

    @Setter
    private LocalClientConnection connection;

    public void registerProcessor(final @NonNull PacketProcessor processor) {
        connection.getChannel().registerProcessor(processor);
    }

    private LocalClientConnection getConnection() {
        if (connection == null || !connection.isConnected()) {
            throw new IllegalStateException("Connection wasn't established");
        }

        return connection;
    }

    public boolean isActive() {
        return connection != null && connection.isConnected();
    }

    public void sendPacket(Packet<?> packet) {
        getConnection().sendPacket(packet);
    }

    public static <A extends Packet.Response<?>, B extends Packet.Request<?, A>> CompletableFuture<A> awaitPacket(@NonNull B packet) {
        return connection.awaitPacket(packet);
    }

    public static <A extends Packet.Response<?>, B extends Packet.Request<?, A>> CompletableFuture<A> awaitPacket(@NonNull B packet, long timeout) {
        return connection.awaitPacket(packet, timeout);
    }
}