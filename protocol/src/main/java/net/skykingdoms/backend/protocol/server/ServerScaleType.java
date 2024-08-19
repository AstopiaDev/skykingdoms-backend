package net.skykingdoms.backend.protocol.server;

public enum ServerScaleType {

    /**
     * Small instance instances that don't require many resources.
     * Example: bedwars game instance instance.
     */
    MINI,

    /**
     * Medium instance instances that host multiple minigames or arenas.
     * Example: multiple arenas on one game instance.
     */
    MEDIUM,

    /**
     * Large instance instances that are resource-intensive and can hold up to 100 players.
     * Example: Mega skywars game.
     */
    LARGE,

    /**
     * Proxy instance instance that is used to connect to other servers.
     * Example: Velocity proxy instance.
     */
    PROXY,

    /**
     * This shouldn't be actually used.
     * Currently, referenced for PROXY WrappedServerTransmittedInfo.
     * It represents any undefined instance instance that is registered in network with custom name.
     */
    UNDEFINED;

    public boolean hasGameMode() {
        return this == MINI || this == MEDIUM || this == LARGE;
    }

    public static final ServerScaleType[] VALUES = values();
}