package net.skykingdoms.backend.megamind.instance.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.server.*;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basically, segment of MEDIUM or LARGE instance, isolated from others
 * in the same instance instance. It always contains an arena instance.
 *
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public final class ArenaInstance extends GameInstance {
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
     * Linked game (arena) ID
     */
    @Getter @Setter
    int gameId;

    final Set<Gamer> gamers = ConcurrentHashMap.newKeySet();

    public ArenaInstance(final @NonNull String name,
                         final @NonNull GameMode gameMode,
                         final @NonNull RemoteClientConnection connection,
                         final @NonNull GameType gameType,
                         final NodeInstance deploymentNode,
                         final @NonNull InetSocketAddress host,
                         final @NonNull ServerScaleType scaleType,
                         final String map,
                         final GameState currentState,
                         final int gameId) {
        super(name, gameMode, connection, gameType, deploymentNode, host, scaleType);
        this.map = map;
        this.currentState = currentState;
        this.gameId = gameId;
    }

    @Override
    public int[] getPlayerIds() {
        return gamers.stream().mapToInt(AbstractGamer::getGamerId).toArray();
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
}
