package net.skykingdoms.backend.megamind.instance.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.server.*;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class LobbyInstance extends BukkitInstance {

    public LobbyInstance(final @NonNull String name,
                         final @NonNull RemoteClientConnection connection,
                         final @NonNull GameType gameType,
                         final NodeInstance deploymentNode,
                         final @NonNull InetSocketAddress host,
                         final int slots) {
        super(
                name,
                connection,
                gameType,
                ServerScaleType.MINI,
                deploymentNode,
                host
        );
        this.slots = slots;
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
    public @Nullable GameMode getGameMode() {
        return null;
    }
}
