package net.skykingdoms.api.protocol.server;


public enum ServerScaleType {

    /**
     * Small server instances that don't require many resources
     * Example: bedwars game server instance
     */
    MINI,

    /**
     * Big server instances that require many resources
     * Example: SoupPvP FFA arena (only some instances are launched "network-wide")
     */
    MEGA,

    /**
     * Lobby server instances are registered for every single game mode (if required)
     * Example: bedwarslobby13
     */
    LOBBY,
    PERSISTENT,

    /**
     * This shouldn't be actually used.
     * Currently, referenced for PROXY WrappedServerTransmittedInfo.
     * It represents any undefined server instance that is registered in network with custom name.
     */
    UNDEFINED

    ;

    public boolean hasGameMode() {
        return this == MINI || this == MEGA || this == PERSISTENT;
    }

    public static final ServerScaleType[] VALUES = values();
}
