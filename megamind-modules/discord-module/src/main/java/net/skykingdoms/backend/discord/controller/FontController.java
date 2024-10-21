package net.skykingdoms.backend.discord.controller;

import net.skykingdoms.backend.DiscordPlugin;
import net.skykingdoms.backend.megamind.MegamindApp;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public enum FontController {
    INSTANCE;

    private Map<String, Font> fontCache = new HashMap<>();

    public Font getFont(String name) {
        return fontCache.get(name);
    }


    public void init() {
        try {
            fontCache.put("minecraft", Font.createFont(Font.TRUETYPE_FONT, DiscordPlugin.INSTANCE.getPlugin().getDataFolder().toPath().resolve("minecraft.ttf").toFile()).deriveFont(24f));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }
}
