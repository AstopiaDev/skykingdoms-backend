package net.skykingdoms.backend.megamind.server.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteClientChannel;
import net.skykingdoms.backend.protocol.server.ILinkedServer;
import net.skykingdoms.backend.protocol.server.INodeServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * GameType is stored in {@link #parentServer} instance.
 * 
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class LobbyServer extends AbstractScalable implements ILinkedServer {

    /**
     * Dedicated server that contains this virtual lobby.
     * {@link GameMode GameMode} is NOT defined in BukkitServer instance!!!!!
     */
    BukkitServer parentServer;

    public LobbyServer(final @NonNull String name,
                       final @NonNull RemoteClientConnection connection,
                       final @NonNull BukkitServer parentServer,
                       final int slots) {
        super(name, connection);
        this.slots = slots;
        this.parentServer = parentServer;
    }

    @Override
    public @NotNull AbstractRemoteClientChannel getChannel() {
        return parentServer.getChannel();
    }

    @Override
    public boolean addPlayer(@NonNull Gamer gamer) {
        return super.addPlayer(gamer);
    }

    @Override
    public boolean removePlayer(@NonNull Gamer gamer) {
        return super.removePlayer(gamer);
    }

    @Override
    public INodeServer getNode() {
        return parentServer.getDeploymentNode();
    }

    @Override
    public ServerScaleType getServerScale() {
        return ServerScaleType.LOBBY;
    }

    @Override
    public GameType getGameType() {
        return parentServer.getGameType();
    }

    @Override
    public @Nullable GameMode getGameMode() {
        return null;
    }
}
