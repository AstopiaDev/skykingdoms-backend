package net.skykingdoms.backend.protocol.type.reconnect.impl.bukkit;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.server.GameMode;
import net.skykingdoms.backend.protocol.server.GameType;
import net.skykingdoms.backend.protocol.server.ServerScaleType;

@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class FluentGameDataImpl extends FluentBukkitDataImpl {
    @NonNull final GameMode gameMode;

    public FluentGameDataImpl(final @NonNull String serverName,
                              final @NonNull String nodeName,
                              final @NonNull ServerScaleType serverScaleType,
                              final @NonNull GameType gameType,
                              final @NonNull String host,
                              final int port,
                              final @NonNull GameMode gameMode) {
        super(serverName, nodeName, serverScaleType, gameType, host, port);
        this.gameMode = gameMode;
    }
}
