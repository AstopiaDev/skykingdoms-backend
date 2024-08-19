package net.skykingdoms.backend.protocol.type.reconnect.impl.bukkit;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.server.GameMode;
import net.skykingdoms.backend.protocol.server.GameState;
import net.skykingdoms.backend.protocol.server.GameType;
import net.skykingdoms.backend.protocol.server.ServerScaleType;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class FluentArenaDataImpl extends FluentGameDataImpl {
    @NonNull final String map;
    @NonNull final GameState gameState;
    final int gameId;

    public FluentArenaDataImpl(final @NonNull String serverName,
                               final @NonNull String nodeName,
                               final @NonNull ServerScaleType serverScaleType,
                               final @NonNull GameType gameType,
                               final @NonNull String host,
                               final int port,
                               final @NonNull GameMode gameMode,
                               final @NonNull String map,
                               final @NonNull GameState gameState,
                               final int gameId) {
        super(serverName, nodeName, serverScaleType, gameType, host, port, gameMode);
        this.map = map;
        this.gameState = gameState;
        this.gameId = gameId;
    }
}
