package net.skykingdoms.api.backend.gamer;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.language.ILanguage;
import net.skykingdoms.api.backend.permission.IUserRank;
import net.skykingdoms.api.backend.permission.UserRankContainer;
import net.skykingdoms.api.backend.setting.Setting;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.backend.setting.impl.EnumSetting;
import net.skykingdoms.api.backend.type.property.PropertyType;
import net.skykingdoms.api.backend.util.map.CachingIndexMap;
import net.skykingdoms.api.backend.util.map.IndexMap;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class ApiGamer implements IGamer {
    /**
     * Unique ID of player in database
     */
    @Setter
    int gamerId;

    /**
     * Player's name
     */
    @Setter
    String name;

    /**
     * Mojang's player UUID
     */
    @Setter
    UUID playerUniqueId;

    /**
     * Real address of player
     */
    @Setter
    InetAddress address;

    /**
     * Player groups container
     */
    @Setter
    UserRankContainer<? extends IUserRank> ranks;

    /**
     * Player's language
     */
    @Setter
    ILanguage language;

    /**
     * User's minecraft protocol version
     */
    @Setter
    int protocol;

    /**
     * User's login status
     */
    @Setter
    boolean loggedIn;

    /**
     * User's premium status
     */
    @Setter
    boolean premium;

    /**
     * Player settings
     */
    @Setter
    EnumMap<SettingType, Setting<?, ?>> settings;

    /**
     * User's properties
     */
    Object2ObjectMap<String, PropertyType<?>> properties;

    public ApiGamer(final @NonNull UUID uuid) {
        this.playerUniqueId = uuid;
    }

    public ApiGamer(final int gamerId) {
        this.gamerId = gamerId;
    }

    @Deprecated
    public ApiGamer(final String name) {
        this.name = name;
    }

    @Override
    public int getGamerId() {
        return gamerId;
    }

    @Override
    public @NotNull UUID getPlayerUniqueId() {
        return playerUniqueId;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull ILanguage getLanguage() {
        return language;
    }

    @Override
    public UserRankContainer<? extends IUserRank> getRanks() {
        return ranks;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    @Override
    public int getProtocol() {
        return protocol;
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public boolean isPremium() {
        return premium;
    }

    public Setting<?, ?> getSetting(final @NonNull SettingType settingType) {
        return settings.get(settingType).clone();
    }

    public Boolean getBooleanSetting(final @NonNull SettingType settingType) {
        return (Boolean) getSetting(settingType).getValue();
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> EnumSetting<T> getEnumSetting(final @NonNull SettingType settingType) {
        return (EnumSetting<T>) getSetting(settingType);
    }

    public abstract void load();
    public abstract void loadSettings();

    public abstract void setSetting(final @NonNull SettingType settingType,
                                    final @NonNull Setting<?, ?> value);

    public abstract <T> void setProperty(final @NonNull String property,
                                     final T value);

    /**
     * Removes property in gamer class and (maybe) drops it
     */
    public abstract void removeProperty(final @NonNull String key);

    /**
     * Removes a property right from database
     */
    public abstract void dropProperty(final @NonNull String key);

    /**
     * Increments a property by {@param value}
     */
    public abstract void addProperty(@NonNull String property, int value);

    public final <T> T getProperty(final @NonNull String key) {
        if (properties == null || !properties.containsKey(key)) {
            return null;
        }

        return (T) properties.get(key).getValue();
    }

    public final <T> T getPropertyOrDefault(final @NonNull String key, T def) {
        if (properties == null || !properties.containsKey(key)) {
            return def;
        }

        return (T) properties.get(key).getValue();
    }

    protected static final IndexMap<IGamer> GAMERS = IndexMap.<IGamer>newMap()
            .registerIndex(String.class, IGamer::getLowerName) // byName
            .registerIndex(UUID.class, IGamer::getPlayerUniqueId) // byUUID
            .registerIntIndex(IGamer::getGamerId); // byId

    protected static final CachingIndexMap<IGamer> OFFLINE_GAMERS = IndexMap.<IGamer>newCachingMap(30, TimeUnit.MINUTES)
            .registerIndex(String.class, IGamer::getLowerName) // byName
            .registerIndex(UUID.class, IGamer::getPlayerUniqueId) // byUUID
            .registerIntIndex(IGamer::getGamerId); // byId

    @SuppressWarnings("unchecked")
    public static <G extends IGamer> G getGamer(final @NonNull String name) {
        synchronized (GAMERS) {
            return (G) GAMERS.get(String.class, name.toLowerCase());
        }
    }
    @SuppressWarnings("unchecked")
    public static <G extends IGamer> G getGamer(final @NonNull UUID uuid) {
        synchronized (GAMERS) {
            return (G) GAMERS.get(UUID.class, uuid);
        }
    }

    @SuppressWarnings("unchecked")
    public static <G extends IGamer> G getOfflineGamer(final @NonNull String name) {
        synchronized (OFFLINE_GAMERS) {
            OFFLINE_GAMERS.cleanUp();

            return (G) OFFLINE_GAMERS.get(String.class, name.toLowerCase());
        }
    }

    @SuppressWarnings("unchecked")
    public static <G extends IGamer> G getOfflineGamer(final @NonNull UUID uuid) {
        synchronized (OFFLINE_GAMERS) {
            OFFLINE_GAMERS.cleanUp();

            return (G) OFFLINE_GAMERS.get(UUID.class, uuid);
        }
    }

    @SuppressWarnings("unchecked")
    public static <G extends IGamer> G getOfflineGamer(final int playerId) {
        synchronized (OFFLINE_GAMERS) {
            OFFLINE_GAMERS.cleanUp();

            return (G) OFFLINE_GAMERS.get(playerId);
        }
    }

    @SuppressWarnings("unchecked")
    public static <G extends IGamer> G getGamer(final int playerId) {
        synchronized (GAMERS) {
            return (G) GAMERS.get(playerId);
        }
    }

    @SuppressWarnings("unchecked")
    public static <G extends IGamer> void forEach(final @NonNull Consumer<G> gamerConsumer) {
        synchronized (GAMERS) {
            GAMERS.values().forEach(t -> gamerConsumer.accept((G) t));
        }
    }

    @SuppressWarnings("unchecked")
    public static <G extends IGamer> Collection<G> getGamers() {
        synchronized (GAMERS) {
            return (Collection<G>) Collections.unmodifiableCollection(GAMERS.values());
        }
    }

    public static List<IGamer> getGamersByIp(final @NonNull InetAddress address) {
        synchronized (GAMERS) {
            return GAMERS.values().stream()
                    .filter(player -> player.getAddress().equals(address))
                    .collect(Collectors.toList());
        }
    }

/*    @SuppressWarnings("unchecked")
    public static <G extends IGamer> List<G> getGamersByGroup(final @NonNull Group group) {
        synchronized (GAMERS) {
            return (List<G>) GAMERS.values().stream()
                    .filter(player -> player.getRanks().canApply(group))
                    .collect(Collectors.toList());
        }
    }*/

    public static <G extends IGamer> void addGamer(final @NonNull G gamer) {
        synchronized (GAMERS) {
            GAMERS.store(gamer);
        }
    }

    public static <G extends IGamer> void invalidateOfflinePlayer(final @NonNull G gamer) {
        synchronized (OFFLINE_GAMERS) {
            OFFLINE_GAMERS.remove(gamer);
        }
    }

    public static int getTotalOnline() {
        synchronized (GAMERS) {
            return GAMERS.size();
        }
    }
}
