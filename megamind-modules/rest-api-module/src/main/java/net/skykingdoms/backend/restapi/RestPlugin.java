package net.skykingdoms.backend.restapi;

import net.skykingdoms.backend.megamind.plugin.Plugin;
import net.skykingdoms.backend.restapi.manager.RestApiManager;

public final class RestPlugin extends Plugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        RestApiManager.INSTANCE.initialize(getConfig().getInt("server-port"));
    }

    @Override
    public void onDisable() {
        RestApiManager.INSTANCE.shutdown();
    }
}
