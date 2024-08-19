package net.skykingdoms.backend.protocol.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.server.impl.ScalableDataImpl;
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
    BEDWARS("Bed Wars", true),

    // SKYWARS
    SOLO_NORMAL("Solo Normal"),
    SOLO_INSANE("Solo Insane"),
    TEAMS_NORMAL("Teams Normal"),
    TEAMS_INSANE("Teams Insane"),
    RANKED_NORMAL("Ranked"),
    MEGA_NORMAL("Mega"),
    SKYWARS("Sky Wars", true),

    MURDER("Murder People", true),
    SKYBLOCK("Sky Block", true),
    FACTIONS("Factions", true),


    // STATIC
    SURVIVAL_BEES("Survival Bees", true),
    SKYPVP("SkyPvP", true),
    BUILD("Build", true),
    DEV("Dev", true),

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
     * By default, we initialize mode with MINI instance
     */
    GameMode(final @NonNull String humanName) {
        staticGame = false;
        this.humanName = humanName;
    }


    GameMode(final @NonNull String humanName, final @NonNull boolean staticGame) {
        this.staticGame = staticGame;
        this.humanName = humanName;
    }

    public static final GameMode[] VALUES = values();

    /**
     * Which scale should be used to instantiate this game mode
     * We couldn't just define it in {@link GameType} because
     * skywars, for example, uses MEGA scale for MEGA_INSANE
     */
    @Getter boolean staticGame;

    public ScalableDataImpl bakeData() {
        return new ScalableDataImpl(ServerScaleType.MEDIUM, GameType.getLinkedGameType(this), this);
    }

}
