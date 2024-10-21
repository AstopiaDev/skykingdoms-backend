package net.skykingdoms.backend.boot;

import net.skykingdoms.backend.DiscordPlugin;
import net.skykingdoms.backend.discord.DiscordBot;
import net.skykingdoms.backend.megamind.plugin.Plugin;

public class DiscordPluginBoot extends Plugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        DiscordPlugin.INSTANCE.init(this);
        DiscordBot.INSTANCE.init();
    }

    @Override
    public void onDisable() {
    }
}