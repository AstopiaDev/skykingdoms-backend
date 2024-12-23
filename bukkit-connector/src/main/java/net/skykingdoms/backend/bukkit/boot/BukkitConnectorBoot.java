package net.skykingdoms.backend.bukkit.boot;

import net.skykingdoms.backend.bukkit.BukkitConnector;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitConnectorBoot extends JavaPlugin {

    public BukkitConnectorBoot() {
        BukkitConnector.INSTANCE.construct(this);
    }

    @Override
    public void onLoad() {
        BukkitConnector.INSTANCE.load();
    }

    @Override
    public void onEnable() {
        BukkitConnector.INSTANCE.init();
    }

    @Override
    public void onDisable() {
        BukkitConnector.INSTANCE.shutdown();
    }
}
