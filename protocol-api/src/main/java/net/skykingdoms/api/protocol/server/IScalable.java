package net.skykingdoms.api.protocol.server;

import net.skykingdoms.api.protocol.type.GameMode;
import org.jetbrains.annotations.Nullable;


public interface IScalable {

    /**
     * Returns server scale type
     */
    ServerScaleType getServerScale();

    /**
     * Returns game type
     */
    GameType getGameType();

    /**
     * returns null if {@link ServerScaleType#hasGameMode()} is false
     */
    @Nullable GameMode getGameMode();
}
