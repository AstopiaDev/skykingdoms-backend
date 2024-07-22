package net.skykingdoms.backend.velocity.gamer;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import gnu.trove.TCollections;
import gnu.trove.map.TObjectIntMap;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.data.settings.Setting;
import net.skykingdoms.backend.base.data.settings.SettingType;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.gamer.IGamer;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.play.B102SetProperty;
import net.skykingdoms.backend.protocol.packet.play.B103RemoveProperty;
import net.skykingdoms.backend.protocol.packet.play.B109UpdateSetting;
import net.skykingdoms.backend.protocol.type.PropertyType;
import net.skykingdoms.backend.velocity.VelocityConnector;
import net.skykingdoms.backend.velocity.command.ProxiedCommandSender;
import net.skykingdoms.backend.velocity.connection.Connection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Proxy-sided realization of {@link IGamer}, providing all necessary information about
 * player.
 *
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public final class Gamer extends AbstractGamer implements IGamer, ProxiedCommandSender {

    /**
     * ProxiedPlayer instance.
     * NULL if not connected
     */
    @Setter volatile Player player;

    /**
     * Which domain player used to connect to the server
     */
    @Setter String domain;

    /**
     * Minecraft protocol version ID
     */
    @Setter int protocol;

    /**
     * Shit used in some cocky cocks
     */
    @Getter @Setter
    boolean connectedToCore;

    /**
     * Timestamp of data load
     */
    final long loginTimestamp = System.currentTimeMillis();

    /**
     * If the player is logged in coordinator
     */
    @Setter boolean syncedWithCoordinator;

    public Gamer(final @NonNull UUID uuid) {
        super(uuid);
    }

    public boolean updateName(final @NonNull String name) {
        if (gamerId < 0 || this.name.equals(name)) {
            return false;
        }

        this.name = name;

        // Persisting changes in database
        NetworkSqlHandler.INSTANCE.updatePlayerName(gamerId, name);

        return true;
    }


    @Override
    public void setSetting(final @NonNull SettingType setting,
                           final @NotNull Setting<?, ?> value) {
        if (updateSetting(setting, value)) {
            Connection.sendPacket(new B109UpdateSetting(gamerId, setting.name(), String.valueOf(setting.getSetting().serialize())));
        }
    }

    public boolean updateSetting(final @NonNull SettingType property,
                                 final @NotNull Setting<?, ?> value) {
        if (properties == null) {
            return false;
        }

        settings.replace(property, value);
        return true;
    }

    // region Properties

    public TObjectIntMap<PropertyType> getProperties() {
        return TCollections.unmodifiableMap(properties);
    }

    public boolean hasProperty(final @NonNull String property) {
        return properties != null && properties.containsKey(property.toLowerCase());
    }

    @Override
    public void addProperty(final @NonNull PropertyType property,
                            final int value) {
        if (properties == null) {
            return;
        }

        this.updateProperty(property, properties.get(property) + value);
    }

    @Override
    public void setProperty(final @NonNull PropertyType property,
                            final int value) {
        if (updateProperty(property, value)) {
            Connection.sendPacket(new B102SetProperty(gamerId, property, value));
        }
    }

    public boolean updateProperty(final @NonNull PropertyType property,
                                  final int value) {
        if (properties == null) {
            return false;
        }

        properties.put(property, value);
        return true;
    }

    public void dropProperty(final @NonNull PropertyType property) {
        if (properties != null) {
            properties.remove(property);
        }
    }

    public void removeProperty(@NonNull PropertyType property) {
        dropProperty(property);

        if (hasPlayerId()) {
            Connection.sendPacket(new B103RemoveProperty(gamerId, property));
        }
    }

    // endregion


    // region language

    public void changeLanguage(final @NonNull Language language) {
        changeLanguage(language, true);
    }

    public void changeLanguage(final @NonNull Language language,
                               final boolean updateOnCore) {
        this.language = language;

        if (updateOnCore) {
            Connection.updateLanguage(gamerId, language);
        }
    }

    // endregion

    // region ranks

/*    @Deprecated
    public void addRank(final @NonNull Group group,
                        final @Nullable LocalDateTime expires) {
        RankSqlHandler.INSTANCE.addUserRank(gamerId, group, expires).thenAccept(this::addRankAndUpdate);
    }*/

    // lol

/*    public void addRank(final @NonNull UserRank rank) {
        ranks.add(rank);
    }

    @Deprecated
    public void addRankAndUpdate(@NonNull UserRank rank) {
        addRank(rank);

        Connection.updateRank(gamerId, B101UpdateRank.Action.ADD_RANK, rank);
    }

    public void removeRank(@NonNull UserRank rank) {
        ranks.remove(rank);
    }

    @Deprecated
    public void removeRankAndUpdate(@NonNull UserRank rank) {
        removeRank(rank);

        Connection.updateRank(gamerId, B101UpdateRank.Action.REMOVE_RANK, rank);
    }*/

    // endregion

    public boolean hasPlayerId() {
        return gamerId > 0;
    }

    public void load() {
        super.load();
        // add more ...
    }

    public boolean isOnline() {
        return player != null && player.isActive();
    }

    @Override
    public CommandSource getCommandSender() {
        return player;
    }

    @Override
    public boolean isConsole() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Gamer)) {
            return false;
        }

        Gamer gamer = (Gamer) o;

        return gamer.gamerId == gamerId;
    }

    // ---------------------------------------------------

    // relay
    public static Collection<Gamer> getGamers() {
        return AbstractGamer.getGamers();
    }

    // relay
    public static Gamer getGamer(Player player) {
        return getGamer(player.getUsername());
    }

    // relay
    public static Gamer getGamer(final @NonNull String name) {
        return AbstractGamer.getGamer(name);
    }

    // relay
    public static Gamer getGamer(final @NonNull UUID uuid) {
        return AbstractGamer.getGamer(uuid);
    }

    // relay
    public static Gamer getGamer(final int playerID) {
        return AbstractGamer.getGamer(playerID);
    }

    @Nullable
    public static Gamer getOrCreate(final @NonNull UUID uuid,
                                    final Consumer<Gamer> gamerConsumer) {
        val gamer = getGamer(uuid);

        if (gamer != null) {
            return gamer;
        }

        return getOrCreate(NetworkSqlHandler.INSTANCE.getPlayerId(uuid), gamerConsumer);
    }

    @NonNull
    public static Gamer getOrCreate(final @NonNull String name,
                                    final Consumer<Gamer> gamerConsumer) {
        val gamer = getGamer(name);

        if (gamer != null) {
            return gamer;
        }

        return getOrCreate(NetworkSqlHandler.INSTANCE.getPlayerId(name), gamerConsumer);
    }

    public static Gamer getOrCreate(final int playerId,
                                    final @NonNull Consumer<Gamer> gamerConsumer) {
        if (playerId < 0) {
            return null;
        }

        Gamer gamer = getGamer(playerId);

        if (gamer != null) {
            return gamer;
        }

        OFFLINE_GAMERS.cleanUp();

        //получаем оффлайн-игрока по этому id
        gamer = getOfflineGamer(playerId);

        if (gamer == null) {
            val playerUuid = NetworkSqlHandler.INSTANCE.getUniqueId(playerId);
            val playerName = NetworkSqlHandler.INSTANCE.getPlayerName(playerId);

            if (playerUuid == null) {
                return null;
            }

            gamer = new Gamer(playerUuid);
            gamer.setName(playerName);

            OFFLINE_GAMERS.store(gamer);
        }

        gamerConsumer.accept(gamer);

        return gamer;
    }

    // relay
    public static Gamer getOfflineGamer(@NonNull String name) {
        return AbstractGamer.getOfflineGamer(name);
    }

    // relay
    public static Gamer getOfflineGamer(int playerId) {
        return AbstractGamer.getOfflineGamer(playerId);
    }

    // relay
    public static void addGamer(Gamer gamer) {
        AbstractGamer.addGamer(gamer);
    }

    // non-relay
    public static void removeGamer(Gamer gamer) {
        GAMERS.remove(gamer);
    }

    // endregion

    // region messages

    @Override
    public void sendLocalizedMessage(boolean modern, @NonNull String text, Object... o) {
        sendMessage(getLanguage().getMessage(isModern(), text, o));
    }

    @Override
    public void sendMessage(String text) {
        if (player != null) {
            player.sendMessage(Component.text(text));
        }
    }

    public void sendJsonMessage(@NonNull String text) {
        if (player != null) {
            player.sendMessage(Component.text(text));
        }
    }

    public void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player != null) {

            player.showTitle(Title.title(Component.text(title), Component.text(subtitle), Title.Times.times(Duration.ofSeconds(fadeIn / 20), Duration.ofSeconds(stay / 20), Duration.ofSeconds(fadeOut / 20))));
        }
    }

    @Override
    public void disconnect(@NotNull String reason) {

    }

    @Override
    public void sendToServer(@NotNull Packet<?> packet) {

    }

    @Override
    public void sendToProxy(@NotNull Packet<?> packet) {

    }

    public void sendLocalizedTitle(final int fadeIn,
                                   final int stay,
                                   final int fadeOut,
                                   final @NonNull String titleKey,
                                   final @NonNull String subtitleKey,
                                   final Object... objects) {
        sendTitle(getLanguage().getMessage(isModern(), titleKey, objects), getLanguage().getMessage(isModern(), subtitleKey, objects), fadeIn, stay, fadeOut);
    }

    public void sendLocalizedTitle(final @NonNull String titleKey,
                                   final @NonNull String subtitleKey,
                                   final Object... objects) {
        sendTitle(getLanguage().getMessage(isModern(), titleKey, objects), getLanguage().getMessage(isModern(), subtitleKey, objects));
    }

    @Override
    public void dispatchCommand(final @NonNull String command) {
        if (player != null) {
            VelocityConnector.INSTANCE.server().getCommandManager().executeAsync(player, command);
        }
    }

    // endregion
}