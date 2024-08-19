package net.skykingdoms.backend.protocol.server;

import org.jetbrains.annotations.Nullable;


public interface IScalable {

    /**
     * Returns instance scale type
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
