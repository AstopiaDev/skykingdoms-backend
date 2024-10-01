package net.skykingdoms.backend.megamind.server.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.GameState;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteClientChannel;
import net.skykingdoms.backend.protocol.server.ILinkedServer;
import net.skykingdoms.backend.protocol.server.INodeServer;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basically, segment of MINI or MEGA server, isolated from others
 * in the same server instance. It always contains a game instance.
 * Example of namings:
 * - mini1A (server instance "mini1" - arena "A" [1])
 * - mega9C (server instance "mega9" - arena "C" [3])
 *
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public final class GameServer extends AbstractScalable implements ILinkedServer {

    /**
     * Dedicated server that contains this arena segment.
     * {@link GameType GameType} and {@link GameMode GameMode}
     * are also defined in BukkitServer instance!
     */
    final BukkitServer parentServer;

    /**
     * A map name for this game session
     */
    final String map;

    /**
     * Game state
     */
    @Setter
    GameState currentState;

    /**
     * Game mode
     */
    @Getter @Setter
    GameMode gameMode;

    /**
     * Linked game (arena) ID
     */
    @Getter @Setter
    int gameId;

    final Set<Gamer> gamers = ConcurrentHashMap.newKeySet();

    public GameServer(final @NonNull String name,
                      final @NonNull GameMode gameMode,
                      final @NonNull RemoteClientConnection connection,
                      final BukkitServer parentServer,
                      final String map,
                      final GameState currentState,
                      final int gameId) {
        super(name, connection);
        this.parentServer = parentServer;
        this.map = map;
        this.currentState = currentState;
        this.gameMode = gameMode;
        this.gameId = gameId;
    }

    @Override
    public @NotNull AbstractRemoteClientChannel getChannel() {
        return parentServer.getChannel();
    }

    @Override
    public int[] getPlayerIds() {
        return gamers.stream().mapToInt(CommonGamer::getGamerId).toArray();
    }

    @Override
    public boolean addPlayer(@NonNull Gamer gamer) {
        gamers.add(gamer);
        return super.addPlayer(gamer);
    }

    @Override
    public boolean removePlayer(@NonNull Gamer gamer) {
        gamers.remove(gamer);
        return super.removePlayer(gamer);
    }

    @Override
    public INodeServer getNode() {
        return parentServer.getDeploymentNode();
    }

    @Override
    public ServerScaleType getServerScale() {
        return parentServer.getScaleType();
    }

    @Override
    public GameType getGameType() {
        return parentServer.getGameType();
    }
}
