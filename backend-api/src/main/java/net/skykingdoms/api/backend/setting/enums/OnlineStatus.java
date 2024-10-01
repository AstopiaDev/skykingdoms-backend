package net.skykingdoms.api.backend.setting.enums;

import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 */
public enum OnlineStatus {

    ONLINE,
    AWAY,
    BUSY,
    INVISIBLE

    ;

    public static final OnlineStatus[] VALUES = values();

    public @NotNull String getNameLocalizationKey() {
        return "onlinestatus_" + name() + "_name";
    }

    public @NotNull String getLoreLocalizationKey() {
        return "onlinestatus_" + name() + "_lore";
    }
}
