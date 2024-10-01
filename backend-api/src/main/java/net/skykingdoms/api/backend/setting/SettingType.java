package net.skykingdoms.api.backend.setting;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.setting.enums.*;
import net.skykingdoms.api.backend.setting.impl.BooleanSetting;
import net.skykingdoms.api.backend.setting.impl.EnumSetting;

import java.util.Arrays;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum SettingType {

    /**
     * Lobby settings
     */
    PLAYERS_VISIBILITY(new BooleanSetting(true)),
    PET_VISIBILITY(new BooleanSetting(true)),
    AUTO_SPAWN_PET(new BooleanSetting(true)),
    LOBBY_DARK_MODE(new BooleanSetting(false)),

    /**
     * API settings
     */
    API_ONLINE_STATUS(new BooleanSetting(true)),
    API_RECENT_GAMES(new BooleanSetting(true)),
    API_WINSTREAKS(new BooleanSetting(true)),

    /**
     * Personal guild settings
     */
    GUILD_NOTIFICATIONS(new BooleanSetting(true)),
    GUILD_SHOW_ONLINE_ONLY(new BooleanSetting(false)),

    /**
     * Game settings
     */
    PARTICLE_QUALITY(new EnumSetting<>(ParticleQuality.MEDIUM)),
    LOBBY_PROTECTION(new BooleanSetting(false)),
    BLOOD_AND_GORE(new BooleanSetting(true)),
    MAP_SELECTOR(new BooleanSetting(true)),
    AUTO_QUEUE(new BooleanSetting(false)),
    LEGACY(new BooleanSetting(true)),

    /**
     * ChatSettings
     */
    CHAT_VISIBILITY(new BooleanSetting(true)),
    GUILD_CHAT_VISIBILITY(new BooleanSetting(true)),
    LOBBY_JOIN_MESSAGE(new BooleanSetting(true)),

    PUBLIC_CHAT_FILTER(new BooleanSetting(true)),
    PARTY_CHAT_FILTER(new BooleanSetting(false)),
    GUILD_CHAT_FILTER(new BooleanSetting(false)),
    PM_CHAT_FILTER(new BooleanSetting(false)),

    PARTY_MESSAGE_SOUNDS(new BooleanSetting(true)),
    GUILD_MESSAGE_SOUNDS(new BooleanSetting(true)),
    PM_SOUNDS(new BooleanSetting(true)),
    CHAT_ALERTS(new BooleanSetting(true)), // @mentions

    /**
     * Social settings
     */

    ONLINE_STATUS(new EnumSetting<>(OnlineStatus.ONLINE)),
    PM_PRIVACY(new EnumSetting<>(PmPrivacy.NONE)),
    FRIEND_REQUEST(new EnumSetting<>(Privacy.NONE)),
    FRIEND_NOTIFICATIONS(new EnumSetting<>(Notifications.ALL)),
    DUEL_INVITE_PRIVACY(new EnumSetting<>(Privacy.NONE)),
    PARTY_INVITE_PRIVACY(new EnumSetting<>(Privacy.NONE)),
    GUILD_INVITE_PRIVACY(new EnumSetting<>(Privacy.NONE)),

    /*
     * Staff settings
     */
    VANISH_ON_JOIN(new BooleanSetting(false)),
    VANISH_ITEM_PICKUP(new BooleanSetting(false))
    ;

    public static final SettingType[] VALUES = values();

    Setting<?, ?> setting;

    public Setting<?, ?> getSetting() {
        return setting.clone();
    }

    public static SettingType valueOf(final @NonNull String name,
                                      final SettingType fallback) {
        return Arrays.stream(VALUES)
                .filter(v -> v.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(fallback);
    }

}
