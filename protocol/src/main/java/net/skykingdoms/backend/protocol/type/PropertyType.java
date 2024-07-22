package net.skykingdoms.backend.protocol.type;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.server.GameType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * This enum represents any existing property type in database.
 * Row names in database equal to {@link #name()} method return value.
 *
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum PropertyType {

    /**
     * Network level experience
     */
    EXPERIENCE,
    ACHIEVEMENT_POINTS,
    PLAYER_POINTS,

    /**
     * This property corresponds to a leveling system.
     * Basically holds a number (level) that player has reward claimed for
     */
    LAST_LEVEL_REWARD_PROPERTY,
    LOGGED_IN,
    VANISHED,
    BUILD_MODE,
    CHAT_MODE,
    TAG_ID,
    PUNCH_MESSAGE_ID,
    DISCORD_ID,
    SOCIAL_SPY_PM,
    SOCIAL_SPY_PC,
    SOCIAL_SPY_CC,

    /*
    Survival
     */
    SURV_COINS,

    /*
    SkyPvP
     */
    SPVP_PRESTIGE,
    ;

    public static PropertyType valueOf(final @NonNull String string,
                                       final PropertyType fallback) {
        return Arrays.stream(VALUES).filter(t -> t.name().equalsIgnoreCase(string))
                .findFirst()
                .orElse(fallback);
    }

    public static @Nullable PropertyType getCoinsProperty(final @NonNull GameType gameType) {
        return valueOf(gameType.getCode().toUpperCase() + "_COINS", null);
    }

    public static final PropertyType[] VALUES = values();

}
