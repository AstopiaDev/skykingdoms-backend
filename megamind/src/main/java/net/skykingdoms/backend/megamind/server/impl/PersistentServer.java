package net.skykingdoms.backend.megamind.server.impl;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
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
 * Represents a persistent static server instance, isolated from others.
 * Each instance contains a dedicated game arena segment.
 * Naming examples:
 * - P1 (persistent server instance "P1")
 * - P2 (persistent server instance "P2")
 *
 * Authored by Vihara.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public final class PersistentServer extends AbstractScalable implements ILinkedServer {

    /**
     * The dedicated server containing this persistent server instance.
     * GameType and GameMode are also defined in the BukkitServer instance.
     */
    final BukkitServer parentServer;

    /**
     * The game mode associated with this server instance.
     */
    @Getter @Setter
    GameType gameType;

    @Getter @Setter
    GameMode gameMode;

    // Set of gamers associated with this persistent server
    final Set<Gamer> gamers = ConcurrentHashMap.newKeySet();

    /**
     * Constructs a PersistentServer instance.
     *
     * @param name           The name of the persistent server instance.
     * @param gameMode       The initial game mode.
     * @param connection     The remote client connection.
     * @param parentServer   The parent BukkitServer instance.
     */
    public PersistentServer(final @NonNull String name,
                            final @NonNull GameMode gameMode,
                            final @NonNull GameType gameType,
                            final @NonNull RemoteClientConnection connection,
                            final BukkitServer parentServer) {
        super(name, connection);
        this.parentServer = parentServer;
        this.gameMode = gameMode;
        this.gameType = gameType;
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