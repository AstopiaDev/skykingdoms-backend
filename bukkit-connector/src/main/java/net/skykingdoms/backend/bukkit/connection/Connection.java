package net.skykingdoms.backend.bukkit.connection;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.play.B101UpdateRank;
import net.skykingdoms.backend.protocol.packet.play.B108UpdatePlayerLanguage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@UtilityClass
public final class Connection {

    @Setter
    private BukkitLocalClientConnection connection;

    public void registerProcessor(final @NonNull PacketProcessor processor) {
        connection.getChannel().registerProcessor(processor);
    }


    private BukkitLocalClientConnection getConnection() {
        if (connection == null || !connection.isConnected()) {
            throw new IllegalStateException("Connection wasn't established");
        }

        return connection;
    }

    public boolean isActive() {
        return connection != null && connection.isConnected();
    }

    public void updateLanguage(int playerId, Language newLanguage) {
        sendPacket(new B108UpdatePlayerLanguage(playerId, newLanguage.getId()));
    }

    public void updateRank(final int playerId,
                           final @NonNull B101UpdateRank.Action action,
                           final @NonNull UserRank rank) {
        sendPacket(new B101UpdateRank(playerId, action, rank.getGroup().getId(), rank.getId(), (int) rank.getExpires(TimeUnit.SECONDS)));
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
