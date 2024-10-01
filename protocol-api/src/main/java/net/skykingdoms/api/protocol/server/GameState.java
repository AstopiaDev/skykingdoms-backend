package net.skykingdoms.api.protocol.server;

/**
 * @author Vihara
 */
public enum GameState {

    STAND_BY, STARTING, GAME, ENDING;

    public static GameState getById(final int id) {
        if (id < 0 || id >= values().length) {
            throw new IllegalStateException("Invalid id");
        }

        return values()[id];
    }

}
