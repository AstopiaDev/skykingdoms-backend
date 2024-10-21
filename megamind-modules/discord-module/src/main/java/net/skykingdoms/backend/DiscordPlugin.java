package net.skykingdoms.backend;

import lombok.Getter;
import net.skykingdoms.backend.boot.DiscordPluginBoot;
import net.skykingdoms.backend.command.LinkMinecraftCommand;
import net.skykingdoms.backend.command.UnlinkMinecraftCommand;
import net.skykingdoms.backend.listener.RankListener;
import net.skykingdoms.backend.megamind.MegamindApp;

@Getter
public enum DiscordPlugin {
    INSTANCE;

    DiscordPluginBoot plugin;

    public void init(DiscordPluginBoot plugin) {
        this.plugin = plugin;
        new RankListener();
        MegamindApp.INSTANCE.commandManager().addCommand(LinkMinecraftCommand.class);
        MegamindApp.INSTANCE.commandManager().addCommand(UnlinkMinecraftCommand.class);
    }
}
