package net.skykingdoms.backend.base.gamer;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.gamer.ApiGamer;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.backend.base.data.auth.AuthSqlHandler;
import net.skykingdoms.backend.base.data.friends.FriendsSqlHandler;
import net.skykingdoms.backend.base.data.friends.FriendshipMeta;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.data.property.PropertySqlHandler;
import net.skykingdoms.backend.base.data.settings.SettingsSqlHandler;
import net.skykingdoms.backend.base.data.tags.TagsSqlHandler;
import net.skykingdoms.backend.base.leveling.Leveling;
import net.skykingdoms.backend.base.permission.RankSqlHandler;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Getter @Setter
public abstract class CommonGamer extends ApiGamer implements IBaseGamer, Serializable<CommonGamer> {

    /**
     * Friend IDs
     */
    Collection<FriendshipMeta> friends;

    public CommonGamer(final @NonNull UUID uuid) {
        super(uuid);
    }

    public CommonGamer(final int gamerId) {
        super(gamerId);
    }

    @Deprecated
    public CommonGamer(final String name) {
        super(name);
    }

    public boolean isVanished() {
        return getPropertyOrDefault("vanished", false);
    }

    public boolean hasBuildMode() {
        return getPropertyOrDefault("build_mode", false);
    }

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

        ranks = RankSqlHandler.INSTANCE.loadUserRanks(gamerId).join();
    }

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

        properties = PropertySqlHandler.INSTANCE.loadProperties(gamerId).join();
    }

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

    public void loadSettings() {
        if (gamerId < 0 || settings != null) {
            return;
        }

        settings = new EnumMap<>(SettingsSqlHandler.INSTANCE.loadSettings(gamerId));
    }

    public boolean hasGamerId() {
        return gamerId > 0;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    public final boolean isStaff() {
        return ranks.canApply(Group.JR_HELPER);
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
     * Basically, kicks/disconnects a player from server
     */
    public abstract void disconnect(final @NotNull String reason);

    /**
     * Sends a packet to bukkit server instance the player is connected to
     */
    public void sendToServer(final @NotNull Packet<?> packet) { }

    /**
     * Sends a packet to proxy server
     */
    public void sendToProxy(final @NotNull Packet<?> packet) { }

    // endregion

    // region leveling

    public int getExperience() {
        return getPropertyOrDefault("experience", 0);
    }

    public int getLevel() {
        return Leveling.getLevel(getExperience());
    }

    public boolean isModern() {
        return getBooleanSetting(SettingType.LEGACY) == Boolean.FALSE;
    }

    public final String getTag() {
        //TagsSqlHandler.INSTANCE.loadAllTags();
        int tagId = getPropertyOrDefault("tag_id", -1);

        if (tagId != -1) {
            if (TagsSqlHandler.INSTANCE.getTag(tagId) == null) {
                return null;
            }
            if (!TagsSqlHandler.INSTANCE.getPlayerTags(playerUniqueId).contains(tagId)) {
                removeProperty("tag_id");
                return null;
            }
            return TagsSqlHandler.INSTANCE.getTag(tagId).name();
        }

        return null;
    }

    // region ranks

    public abstract void addRank(final @NonNull Group group,
                                 final LocalDateTime expires);

    public abstract void addRank(@NonNull UserRank rank);

    public abstract void setRank(@NonNull Group group, final LocalDateTime expires);

    // endregion

    @Override
    public @NotNull String getDisplayName() {
        String displayName = ranks.getPriorityRank().getGroup().getPrefix() + name;
        String tag = getTag();
        if (tag != null) {
            displayName = displayName + " " + tag;
        }

        return displayName;
    }

    public String getColoredName() {
        return ranks.getPriorityRank().getGroup().getPrefixColor() + getName();
    }

    /**
     * Formats groups for gamer
     */
    public String getFormattedRank(final @NonNull Group group,
                                   final boolean brackets) {

        return group.getDisplayName();
    }

    /**
     * Get amount of player points this gamer has
     * @return integer of amount of player points
     */
    public int getPlayerPoints() {
        return getPropertyOrDefault("player_points", 0);
    }

    // endregion
    @Override
    public CommonGamer serialize(@NonNull ByteBuf out) {
        PacketUtils.writeVarInt(out, gamerId);
        PacketUtils.writeString(out, name);
        PacketUtils.writeString(out, playerUniqueId.toString());
        PacketUtils.writeAddress(out, address);
        return this;
    }

    @Override
    public CommonGamer deserialize(@NonNull ByteBuf in) {
        this.gamerId = PacketUtils.readVarInt(in);
        this.name = PacketUtils.readString(in);
        this.playerUniqueId = UUID.fromString(PacketUtils.readString(in));
        try {
            this.address = PacketUtils.readAddress(in);
        } catch (UnknownHostException e) {
            Logging.IMP.error("Unknown host when deserializing a gamer: {}", e);
        }
        return this;
    }

    public static void kickAll() {
        synchronized (GAMERS) {
            for (val gamer : new ArrayList<>(GAMERS.values())) {
                ((CommonGamer) gamer).quit(); // todo: refactor this thing xd
            }
        }
    }
}
