package net.skykingdoms.api.protocol.type;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum GameMode {

    // BEDWARS
    BEDWARS_EIGHT_ONE("Solo"),  // Solo
    BEDWARS_EIGHT_TWO("Doubles"),  // Doubles
    BEDWARS_FOUR_THREE("3v3v3v3"), // 3v3v3v3
    BEDWARS_FOUR_FOUR("4v4v4v4"),  // 4v4v4v4
    BEDWARS_TWO_FOUR("4v4"),   // 4v4
    BEDWARS("Bed Wars", ServerScaleType.PERSISTENT, true),

    // SKYWARS
    SOLO_NORMAL("Solo Normal"),
    SOLO_INSANE("Solo Insane"),
    TEAMS_NORMAL("Teams Normal"),
    TEAMS_INSANE("Teams Insane"),
    RANKED_NORMAL("Ranked"),
    MEGA_NORMAL("Mega", ServerScaleType.MEGA),
    SKYWARS("Sky Wars", ServerScaleType.PERSISTENT, true),

    MURDER("Murder People", ServerScaleType.PERSISTENT, true),
    SKYBLOCK("Sky Block", ServerScaleType.PERSISTENT, true),
    FACTIONS("Factions", ServerScaleType.PERSISTENT, true),


    // STATIC
    SURVIVAL_BEES("Survival Bees", ServerScaleType.PERSISTENT, true),
    SKYPVP("SkyPvP", ServerScaleType.PERSISTENT, true),
    BUILD("Build", ServerScaleType.PERSISTENT, true),
    DEV("Dev", ServerScaleType.PERSISTENT, true),

    // SKYPVP
    SKYPVP_MOB_ARENA_ONE("SkyPvP Mob Arena"),
    SKYPVP_MOB_ARENA_MORE("SkyPvP Mob Arena"),

    // DUELS
    DUELS_SKYPVP_DUEL("SkyPvP Duel"),
    DUELS_CLASSIC_DUEL("Classic Duel")
    //SURVIVAL_COMPETITIVE("Competitive Survival", ServerScaleType.PERSISTENT, true),
    //SURVIVAL_VANILLA("Vanilla Survival", ServerScaleType.PERSISTENT, true),
    ;

    String humanName;

    public static GameMode valueOf(final @Nullable String name,
                                   final @Nullable GameMode fallback) {
        if (name == null) {
            return fallback;
        }

        return Arrays.stream(VALUES)
                .filter(gm -> gm.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(fallback);
    }

    /**
     * By default, we initialize mode with MINI server
     */
    GameMode(final @NonNull String humanName) {
        scaleType = ServerScaleType.MINI;
        staticGame = false;
        this.humanName = humanName;
    }

    GameMode(final @NonNull String humanName, final @NonNull ServerScaleType scaleType) {
        staticGame = false;
        this.scaleType = scaleType;
        this.humanName = humanName;
    }

    GameMode(final @NonNull String humanName, final @NonNull ServerScaleType scaleType, final @NonNull boolean staticGame) {
        this.staticGame = staticGame;
        this.scaleType = scaleType;
        this.humanName = humanName;
    }

    public static final GameMode[] VALUES = values();

    /**
     * Which scale should be used to instantiate this game mode
     * We couldn't just define it in {@link GameType} because
     * skywars, for example, uses MEGA scale for MEGA_INSANE
     */
    @Getter ServerScaleType scaleType;
    @Getter boolean staticGame;

    public ScalableDataImpl bakeData() {
        return new ScalableDataImpl(scaleType, GameType.getLinkedGameType(this), this);
    }

}
