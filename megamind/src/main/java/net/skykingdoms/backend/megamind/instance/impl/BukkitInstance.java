package net.skykingdoms.backend.megamind.instance.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.instance.AbstractScalable;
import net.skykingdoms.backend.protocol.server.GameMode;
import net.skykingdoms.backend.protocol.server.GameType;
import net.skykingdoms.backend.protocol.server.INodeServer;
import net.skykingdoms.backend.protocol.server.ServerScaleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

/**
 * Basically, represents any playable instance registered in the network.
 * @Author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class BukkitInstance extends AbstractScalable {

    /**
     * Which game type this instance hosts
     */
    GameType gameType;

    /**
     * Lobby or game instance or its fucking undefined?!?!
     */
    ServerScaleType scaleType;

    /**
     * A node this instance is deployed on
     */
    NodeInstance deploymentNode;

    /**
     * Remote bukkit's host
     */
    InetSocketAddress host;

    public BukkitInstance(final @NonNull String name,
                          final @NonNull RemoteClientConnection connection,
                          final @NonNull GameType gameType,
                          final @NonNull ServerScaleType scaleType,
                          final NodeInstance deploymentNode,
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
