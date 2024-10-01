package net.skykingdoms.api.backend.setting.enums;

import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 */
public enum Privacy {

    MAX, HIGH, MEDIUM, NONE;

    public static final Privacy[] VALUES = values();

    public @NotNull String getNameLocalizationKey() {
        return "privacy_" + name() + "_name";
    }

    public @NotNull String getLoreLocalizationKey() {
        return "privacy_" + name() + "_lore";
    }

}
