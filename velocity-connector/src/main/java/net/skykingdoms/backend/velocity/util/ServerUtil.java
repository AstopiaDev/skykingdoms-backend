package net.skykingdoms.backend.velocity.util;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.protocol.packet.play.C15GetBestServer;
import net.skykingdoms.backend.velocity.VelocityConnector;
import net.skykingdoms.backend.velocity.connection.Connection;
import net.skykingdoms.backend.velocity.gamer.Gamer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@UtilityClass
public final class ServerUtil {

    /**
     * Basically retrieves any lobby/hub server (used for debug purposes)
     */
    public RegisteredServer getRandomLobbyServer() {
        return VelocityConnector.INSTANCE.server().getAllServers().stream()
                .filter(s -> s.getServerInfo().getName().contains("mainlobby"))
                .findFirst()
                .orElse(null);
    }

    public static CompletableFuture<RegisteredServer> getServerToConnect(final Gamer gamer,
                                                                   final @NonNull ScalableDataImpl scalableData) {
        ProxyServer server = VelocityConnector.INSTANCE.server();

        return ServerUtil.getBestServer(gamer, scalableData)
                .thenCompose(response -> response
                        .map(s -> CompletableFuture.completedFuture(server.getServer(s).orElse(null)))
                        .orElseGet(() -> CompletableFuture.completedFuture(ServerUtil.getRandomLobbyServer()))
        );
    }

    public CompletableFuture<Optional<String>> getBestServer(final @NonNull Gamer gamer,
                                                             final @NonNull ScalableDataImpl scalableData) {
        return Connection.awaitPacket(new C15GetBestServer( gamer.getName(), gamer.getAddress(), scalableData))
                .thenApply(result -> Optional.ofNullable(result.getServer()))
                .exceptionally(cause -> Optional.empty());
    }

}
