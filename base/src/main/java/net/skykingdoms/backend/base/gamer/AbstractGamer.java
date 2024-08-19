package net.skykingdoms.backend.base.gamer;

import gnu.trove.TCollections;
import gnu.trove.map.TObjectIntMap;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.data.friends.FriendsSqlHandler;
import net.skykingdoms.backend.base.data.friends.FriendshipMeta;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.data.property.PropertySqlHandler;
import net.skykingdoms.backend.base.data.settings.Setting;
import net.skykingdoms.backend.base.data.settings.SettingType;
import net.skykingdoms.backend.base.data.settings.SettingsSqlHandler;
import net.skykingdoms.backend.base.data.settings.impl.EnumSetting;
import net.skykingdoms.backend.base.data.tags.TagsSqlHandler;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.leveling.Leveling;
import net.skykingdoms.backend.base.permission.Group;
import net.skykingdoms.backend.base.permission.RankSqlHandler;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.base.util.map.CachingIndexMap;
import net.skykingdoms.backend.base.util.map.IndexMap;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.type.PropertyType;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Getter @Setter
public abstract class AbstractGamer implements IGamer {

    /**
     * Unique ID of player in database
     */
    int gamerId;

    /**
     * Player's name
     */
    String name;

    /**
     * Mojang's player UUID
     */
    UUID playerUniqueId;

    /**
     * Real address of player
     */
    InetAddress address;

    /**
     * Player groups container
     */
    UserRanks ranks;

    /**
     * Player's language
     */
    @Setter @Getter
    Language language;

    /**
     * User's minecraft protocol version
     */
    @Setter int protocol;

    /**
     * User's properties
     */
    TObjectIntMap<PropertyType> properties;

    /**
     * Friend IDs
     */
    Collection<FriendshipMeta> friends;

    /**
     * Player settings
     */
    EnumMap<SettingType, Setting<?, ?>> settings;

    /**
     * Fake player name
     */
/*    @Getter
    FakePlayerNickname fakeNickname;

    *//**
     * This retarded thing with fake user ranks
     *//*
    @Getter
    FakeUserRank fakeUserRank;*/

    // region constructors

    public AbstractGamer(final @NonNull UUID uuid) {
        this.playerUniqueId = uuid;
    }

    public AbstractGamer(final int gamerId) {
        this.gamerId = gamerId;
    }

    @Deprecated
    public AbstractGamer(final String name) {
        this.name = name;
    }

    // endregion

    // region vanish

    public boolean isVanished() {
        return getProperty(PropertyType.VANISHED) == 1;
    }

    public boolean hasBuildMode() {
        return getProperty(PropertyType.BUILD_MODE) == 1;
    }

    // endregion

    // region loaders

    public final void loadLanguage() {
        if (gamerId < 0 || language != null) {
            return;
        }

        language = NetworkSqlHandler.INSTANCE.getPlayerLanguage(gamerId);
    }

    public final void loadPlayerId() {
        if (playerUniqueId == null) {
            playerUniqueId = NetworkSqlHandler.INSTANCE.getUniqueId(name);
        }

        // todo: rewrite shitcode
        if (playerUniqueId == null) {
            gamerId = -1;
            return;
        }

        gamerId = NetworkSqlHandler.INSTANCE.getPlayerId(playerUniqueId);
    }

    public final void loadPlayerName() {
        if (gamerId < 0) {
            return;
        }

        name = NetworkSqlHandler.INSTANCE.getPlayerName(gamerId);
    }

    public final void loadGroups() {
        if (gamerId < 0 || ranks != null) {
            return;
        }

        ranks = RankSqlHandler.INSTANCE.loadUserRanks(playerUniqueId).join();
    }

    // this saves us some brain cells
    public final void loadGroupsAndName() {
        loadGroups();
        loadProperties();
        loadPlayerName();
    }

    public final void loadFriends() {
        if (gamerId < 0 || friends != null) {
            return;
        }

        friends = Collections.synchronizedSet(FriendsSqlHandler.INSTANCE.getFriendships(gamerId));
    }

    public final void loadProperties() {
        if (gamerId < 0 || properties != null) {
            return;
        }

        properties = TCollections.synchronizedMap(PropertySqlHandler.INSTANCE.loadProperties(gamerId).join());
    }

    // endregion

    // region nicks

    // endregion

    // region friends

    public boolean addFriend(final FriendshipMeta meta) {
        if (!hasGamerId() && friends == null) {
            return false;
        }

        if (friends.contains(meta)) {
            return false;
        }

        return friends.add(meta);
    }

    public boolean removeFriend(final int friendId) {
        if (!hasGamerId() || friends == null) {
            return false;
        }


        // do not remove by meta (trust me)
        return friends.removeIf(f -> f.isFriendship(gamerId, friendId));
    }

    public final boolean isFriend(final int friendId) {
        return friends != null && friends.stream().anyMatch(f -> f.isFriendship(gamerId, friendId));
    }

    // endregion

    // region settings

    public void loadSettings() {
        if (gamerId < 0 || settings != null) {
            return;
        }

        settings = new EnumMap<>(SettingsSqlHandler.INSTANCE.loadSettings(gamerId));
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

    public abstract void setSetting(final @NonNull SettingType settingType,
                                    final @NonNull Setting<?, ?> value);

    // region impl

    public boolean hasGamerId() {
        return gamerId > 0;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    public final boolean isStaff() {
        return ranks.canApply(Group.HELPER);
    }

    @Override
    public final int hashCode() {
        return gamerId;
    }

    // endregion

    public void load() {
        loadPlayerId();
        loadPlayerName();
        loadLanguage();
        loadGroups();
        loadProperties();
        loadSettings();
        loadFriends();
       // loadFakeUserRank();
    }

    /**
     * Invoked commonly on PlayerQuit/PlayerDisconnect events
     * WARNING: this is not kick/disconnect method!!!!
     */
    public void quit() { }

    public final void sendTitle(@NonNull String title, @NonNull String subtitle) {
        sendTitle(title, subtitle, 20, 20, 20);
    }

    /**
     * Sends a parameterized title
     */
    public abstract void sendTitle(final @NotNull String title,
                                   final @NotNull String subtitle,
                                   final int fadeIn,
                                   final int stay,
                                   final int fadeOut);

    /**
     * Kicks a player with a reason translated
     */
    public final void localizedDisconnect(final @NonNull String key,
                                          final Object... objects) {
        disconnect(language.getMessage(isModern(), key, objects));
    }

    /**
     * Basically, kicks/disconnects a player from instance
     */
    public abstract void disconnect(final @NotNull String reason);

    /**
     * Sends a packet to bukkit instance instance the player is connected to
     */
    public void sendToServer(final @NotNull Packet<?> packet) { }

    /**
     * Sends a packet to proxy instance
     */
    public void sendToProxy(final @NotNull Packet<?> packet) { }

    // region property

    public final int getProperty(final @NonNull PropertyType key) {
        if (properties == null) {
            return 0;
        }

        return properties.get(key);
    }

    public abstract void setProperty(final @NonNull PropertyType property,
                                     final int value);

    /**
     * Removes property in gamer class and (maybe) drops it
     */
    public abstract void removeProperty(final @NonNull PropertyType key);

    /**
     * Removes a property right from database
     */
    public abstract void dropProperty(final @NonNull PropertyType key);

    /**
     * Increments a property by {@param value}
     */
    public abstract void addProperty(@NonNull PropertyType property, int value);

    // endregion

    // region leveling

    public int getExperience() {
        return getProperty(PropertyType.EXPERIENCE);
    }

    public int getLevel() {
        return Leveling.getLevel(getExperience());
    }

    public boolean isModern() {
        return getBooleanSetting(SettingType.LEGACY) == Boolean.FALSE;
    }

    public final String getTag() {
        //TagsSqlHandler.INSTANCE.loadAllTags();

        if (getProperty(PropertyType.TAG_ID) != -1) {
            if (TagsSqlHandler.INSTANCE.getTag(getProperty(PropertyType.TAG_ID)) == null) {
                return null;
            }
            if (!TagsSqlHandler.INSTANCE.getPlayerTags(playerUniqueId).contains(getProperty(PropertyType.TAG_ID))) {
                setProperty(PropertyType.TAG_ID, -1);
                return null;
            }
            return TagsSqlHandler.INSTANCE.getTag(getProperty(PropertyType.TAG_ID)).name();
        }

        return null;
    }

    // region ranks

/*    public abstract void addRank(final @NonNull Group group,
                                 final LocalDateTime expires);

    public abstract void addRank(@NonNull UserRank rank);*/

    // endregion

    @Override
    public @NotNull String getDisplayName() {

/*        if (fakeUserRank != null) {
            return fakeUserRank.getPrefix() + name;
        }

*//*        // for players that are MVP+ or MVP++ we do "special" processing of display names
        if (ranks.getPriorityRank().getGroup() == Group.CHAMPION || ranks.getPriorityRank().getGroup() == Group.GODLIKE) {
            return _getFormattedMVPShit(ranks.getPriorityRank().getGroup(), true) + name;
        }*//*
*/
        String displayName = ranks.getPriorityRank().getGroup().getPrefix() + name;
        String tag = getTag();
        if (tag != null) {
            displayName = displayName + " " + tag;
        }

        return displayName;
    }

    public String getColoredName() {
/*        if (fakeUserRank != null) {
            return fakeUserRank.getPrefixColor() + name;
        }*/

        return ranks.getPriorityRank().getGroup().getPrefixColor() + getName();
    }

    /**
     * Formats groups for gamer
     */
    public String getFormattedRank(final @NonNull Group group,
                                   final boolean brackets) {
//        if (fakeUserRank != null) {
//            return fakeUserRank.getName();
//        }
/*        if (group == Group.MVP_PLUS || group == Group.SUPERSTAR) {
            return _getFormattedMVPShit(group, brackets);
        }*/

        return group.getDisplayName();
    }

/*    @NotNull
    protected String _getFormattedMVPShit(final Group group,
                                          final boolean brackets) {
        val plusColor = ChatColor.values()[getProperty(PropertyType.MVP_PLUS_COLOR)];
        var prefColor = ChatColor.AQUA;
        if (group == Group.SUPERSTAR) {
            prefColor = getProperty(PropertyType.MVP_COLOR) == 0 ? ChatColor.GOLD : ChatColor.AQUA;
        }

        return prefColor + (brackets ? "[" : "") + "MVP" + plusColor + (group == Group.SUPERSTAR ? "++" : "+") + (brackets ? prefColor + "] " : "");
    }*/

    // region data loaders

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

    public static void kickAll() {
        synchronized (GAMERS) {
            for (val gamer : new ArrayList<>(GAMERS.values())) {
                ((AbstractGamer) gamer).quit(); // todo: refactor this thing xd
            }
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

    /**
     * Get amount of player points this gamer has
     * @return integer of amount of player points
     */
    public int getPlayerPoints() {
        return this.getProperty(PropertyType.PLAYER_POINTS);
    }

    // endregion
}
