package net.skykingdoms.backend.discord.controller;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public enum PlayerController {
    INSTANCE;

    private final BiMap<String, String> linkCodes = HashBiMap.create();

    public void init() {
        System.out.println("PlayerController is ready!");
    }

    public void linkCode(final String discordId, final String code) {
        linkCodes.put(discordId, code);
    }

    public boolean hasDiscordId(final String discordId) {
        return linkCodes.containsKey(discordId);
    }

    public boolean hasCode(final String code) {
        return linkCodes.containsValue(code);
    }

    public String getCode(final String discordId) {
        return linkCodes.get(discordId);
    }

    public String getDiscordId(final String code) {
        return linkCodes.inverse().get(code);
    }

    public void removeCode(final String discordId) {
        linkCodes.remove(discordId);
    }

    public void removeCodeByCode(final String code) {
        linkCodes.inverse().remove(code);
    }
}

