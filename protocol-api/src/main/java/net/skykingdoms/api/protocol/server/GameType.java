package net.skykingdoms.api.protocol.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.api.protocol.type.DeploymentVersion;
import net.skykingdoms.api.protocol.type.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum GameType {

    /**
     * Main HUB doesn't have any game modes linked
     */
    MAIN(new ArrayList<>(), true, "Main", null),
    SURVIVAL(List.of(
            GameMode.SURVIVAL_BEES
    ), true, "Survival", "surv", DeploymentVersion.V1_20_4),
    SKYPVP(List.of(
            GameMode.SKYPVP
    ), false, "SkyPvP", "spvp"),
    SKYWARS(List.of(
            GameMode.SKYWARS
    ), false, "SkyWars", "sw"),
    BEDWARS(List.of(
            GameMode.BEDWARS
    ), false, "BedWars", "bw"),
    SKYBLOCK(List.of(
            GameMode.SKYBLOCK
    ), false, "SkyBlock", "sb"),
    FACTIONS(List.of(
            GameMode.FACTIONS
    ), false, "Factions", "fac"),
    MURDER_MYSTERY(List.of(
            GameMode.MURDER
    ), false, "Murder Mystery", "mur"),
    DUELS(List.of(
            GameMode.DUELS_SKYPVP_DUEL
    ), false, "Duels", "duels"),
    BUILD(List.of(
            GameMode.BUILD
    ), false, "Build", "build"),
    DEV(List.of(
            GameMode.DEV,
            GameMode.SKYPVP_MOB_ARENA_ONE,
            GameMode.SKYPVP_MOB_ARENA_MORE
    ), false, "Secret Game", "dev");

/*    *//**
     * BedWars
     *//*
    BEDWARS(List.of(
            GameMode.BEDWARS_EIGHT_ONE,
            GameMode.BEDWARS_EIGHT_TWO,
            GameMode.BEDWARS_FOUR_THREE,
            GameMode.BEDWARS_FOUR_FOUR,
            GameMode.BEDWARS_TWO_FOUR
    ), true, "BedWars", "bw"),

    *//**
     * SkyWars
     *//*
    SKYWARS(List.of(
        GameMode.SOLO_NORMAL,
        GameMode.SOLO_INSANE,
        GameMode.TEAMS_NORMAL,
        GameMode.TEAMS_INSANE,
        GameMode.RANKED_NORMAL
    ), true, "SkyWars", "sw")*/
    ;

    public static GameType valueOf(final @NonNull String name,
                                   final @Nullable GameType fallback) {
        return Arrays.stream(VALUES)
                .filter(s -> s.name().equalsIgnoreCase(name) || (s.getCode() != null && s.getCode().equalsIgnoreCase(name)))
                .findFirst()
                .orElse(fallback);
    }

    /**
     * All linked game modes to this game type
     */
    Collection<GameMode> modes;

    public String getDescriptionLocalization() {
        return "mode_" + name() + "_description";
    }

    /**
     * Should dynamic lobby instance be created for this game mode
     * Eg: ThePit shouldn't have this enabled
     */
    @Getter boolean hasLobby;

    /**
     * A human-beautified name. eg: BedWars, SkyWars
     */
    @Getter String humanName;

    /**
     * Code for game type
     */
    @Getter String code;

    @Getter DeploymentVersion version;

    GameType(final @NonNull Collection<GameMode> modes,
                    final boolean hasLobby,
                    final @NonNull String humanName,
                    final @Nullable String code) {
        this.modes = modes;
        this.hasLobby = hasLobby;
        this.humanName = humanName;
        this.code = code;
        this.version = DeploymentVersion.V1_8_8;
    }

    /**
     * Returns a game type that owns some gamemode
     */
    public static @Nullable GameType getLinkedGameType(final @NonNull GameMode gameMode) {
        return Arrays.stream(VALUES)
                .filter(gt -> gt.getModes().contains(gameMode))
                .findFirst()
                .orElse(null);
    }

    public ScalableDataImpl bakeLobbyData() {
        return new ScalableDataImpl(ServerScaleType.LOBBY, this, null);
    }

    public static final GameType[] VALUES = values();

    public final @NotNull String getDisplayLocalizationKey() {
        return "network_game_" + name() + "_displayname";
    }
}
