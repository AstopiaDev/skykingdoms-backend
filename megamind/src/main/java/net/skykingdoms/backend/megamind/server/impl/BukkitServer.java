package net.skykingdoms.backend.megamind.server.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.protocol.server.INodeServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

/**
 * Basically, represents any playable server registered in the network.
 *
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class BukkitServer extends AbstractScalable {

    /**
     * Which game type this server hosts
     */
    GameType gameType;

    /**
     * Lobby or game instance or its fucking undefined?!?!
     */
    ServerScaleType scaleType;

    /**
     * A node this server is deployed on
     */
    NodeServer deploymentNode;

    /**
     * Remote bukkit's host
     */
    InetSocketAddress host;

    public BukkitServer(final @NonNull String name,
                        final @NonNull RemoteClientConnection connection,
                        final @NonNull GameType gameType,
                        final @NonNull ServerScaleType scaleType,
                        final NodeServer deploymentNode,
                        final @NonNull InetSocketAddress host) {
        super(name, connection);
        this.gameType = gameType;
        this.scaleType = scaleType;
        this.deploymentNode = deploymentNode;
        this.host = host;
    }

    @Override
    public @NotNull INodeServer getNode() {
        return deploymentNode;
    }

    @Override
    public ServerScaleType getServerScale() {
        return scaleType;
    }

    @Override
    public @Nullable GameMode getGameMode() {
        return null;
    }

}
