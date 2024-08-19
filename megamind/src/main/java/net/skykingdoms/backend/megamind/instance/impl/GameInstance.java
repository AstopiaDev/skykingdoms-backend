package net.skykingdoms.backend.megamind.instance.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.server.*;

import java.net.InetSocketAddress;

/**
 * Basically, segment of MINI or MEGA instance, not isolated from others.
 *
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class GameInstance extends BukkitInstance {

    /**
     * Game mode
     */
    @Getter @Setter
    GameMode gameMode;

    public GameInstance(final @NonNull String name,
                        final @NonNull GameMode gameMode,
                        final @NonNull RemoteClientConnection connection,
                        final @NonNull GameType gameType,
                        final NodeInstance deploymentNode,
                        final @NonNull InetSocketAddress host,
                        final @NonNull ServerScaleType scaleType) {
        super(name, connection, gameType, scaleType, deploymentNode, host);
        this.gameMode = gameMode;
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
