package net.skykingdoms.minimind.gamer;

import net.skykingdoms.minimind.Minimind;

public class GamerMind extends Minimind {
    private static GamerMind instance = null;

    private GamerMind() { super("gamer"); }

    public static GamerMind getInstance() {
        if (instance == null) {
            instance = new GamerMind();
        }
        return instance;
    }

    @Override
    public void preBootstrap() {

    }

    @Override
    public void postBootstrap() {

    }

    @Override
    public void shutdown() {

    }
}
