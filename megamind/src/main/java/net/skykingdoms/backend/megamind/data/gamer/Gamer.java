package net.skykingdoms.backend.megamind.data.gamer;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.gamer.IGamer;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.permission.IUserRank;
import net.skykingdoms.api.backend.permission.UserRankContainer;
import net.skykingdoms.api.backend.setting.Setting;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.backend.type.property.PropertyType;
import net.skykingdoms.api.megamind.event.impl.gamer.*;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.data.auth.AuthSqlHandler;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.data.property.PropertySqlHandler;
import net.skykingdoms.backend.base.data.settings.SettingsSqlHandler;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.permission.RankSqlHandler;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.CommandSender;
import net.skykingdoms.backend.megamind.lookup.LookupManager;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.megamind.server.impl.*;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.play.*;
import net.skykingdoms.backend.protocol.server.IPlayableBukkitServer;
import net.skykingdoms.backend.protocol.type.connect.ConnectStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class Gamer extends CommonGamer implements CommandSender, IMegamindGamer {

    /**
     * Which hostname user used to connect to le server
     */
    @Setter String domain;

    /**
     * Login date
     */
    @Setter LocalDateTime firstLogin;
    @Setter LocalDateTime lastLogin;

    /**
     * If the player is online
     */
    @Setter boolean online;

    /**
     * User's session uuid
     */
    @Setter
    UUID session;

    /**
     * If the player is registered
     */
    @Setter boolean registered;

    /**
     * The proxy which player is connected to
     */
    @Setter ProxyServer proxyServer;

    /**
     * The game instance server player is connected to
     */
    @Setter BukkitServer bukkitServer;

    /**
     * Current game instance server (null if not on game server)
     */
    @Setter GameServer gameServer;

    /**
     * Current lobby server (null if not in lobby)
     */
    @Setter LobbyServer lobbyServer;

    /**
     * Current persistent server (null if not in a persistent server)
     */
    @Setter PersistentServer persistentServer;

    /**
     * A field used to indicate to which server we are currently trying to connect.
     * When not connecting, it equals to NULL :3
     */
    @Setter IPlayableBukkitServer currentlyConnectingTo;

    /**
     * A flag that indicates that player just joined the network
     */
    @Setter boolean justJoined = true;

    public Gamer(@NonNull UUID playerUniqueId) {
        super(playerUniqueId);
    }

    private Gamer(final @NonNull String username) {
        super(username);
    }
    // tab

    public void updateServer(AbstractScalable serverInstance) {


        val oldServer = getCurrentServer();
        if (oldServer == serverInstance) {
            return;
        }

        if (serverInstance != null && serverInstance == currentlyConnectingTo) {
            currentlyConnectingTo = null;
            Logging.IMP.info(serverInstance.getName());
        }

        if (oldServer != null) {
            oldServer.removePlayer(this);
        }

        if (serverInstance == null) {
            setGameServer(null);
            setLobbyServer(null);
            setBukkitServer(null);
            return;
        }

        // Player just joined
        if (oldServer == null) {
            lastLogin = LocalDateTime.now();
        }

        if (serverInstance instanceof BukkitServer) {
            val bs = ((BukkitServer) serverInstance);
            setBukkitServer(bs);
            setGameServer(null);
            setLobbyServer(null);

            bs.addPlayer(this);
        }

        if (serverInstance instanceof GameServer) {
            val gs = (GameServer) serverInstance;
            setBukkitServer(gs.getParentServer());
            setGameServer(gs);
            setLobbyServer(null);

            gs.addPlayer(this);

            val gameRedirectEvent = MegamindApp.INSTANCE.pluginManager().callEvent(new PlayerGameRedirectEvent(this));
/*            for (val addition : gameRedirectEvent.getAdditionalGamerList()) {
                addition.sendTo(gs);
            }*/
        }

        if (serverInstance instanceof LobbyServer) {
            val ls = (LobbyServer) serverInstance;
            setBukkitServer(ls.getParentServer());
            setLobbyServer(ls);
            setGameServer(null);

            ls.addPlayer(this);
        }

        if (serverInstance instanceof PersistentServer) {
            val ps = (PersistentServer) serverInstance;
            setBukkitServer(ps.getParentServer());
            setPersistentServer(ps);
            setGameServer(null);
            setLobbyServer(null);

            ps.addPlayer(this);
        }

        updateTablist();
    }

    public void updateTablist() {
        String header = language.getMessage(isModern(), "tab_header", "%server_name%", getBukkitServer().getName());
        String footer = language.getMessage(isModern(), "tab_footer");

        sendToProxy(new S14SetPlayerTablist(gamerId, header, footer));
    }

    // endregion

    // region ranks

    public String getPrefixedName() {
        return ranks.getPriorityRank().getGroup().getPrefix() + name;
    }

    public void checkExpiredRanks() {
        for (val rank : ranks.getExpiredRanks()) {
            sendLocalizedMessage(isModern(), "group_expired", "%rank%", rank.getGroup().getDisplayName());

            removeRankAndUpdate((UserRank) rank);
        }
    }

    @Override
    public void addRank(final @NonNull Group group,
                        final LocalDateTime expires) {
        RankSqlHandler.INSTANCE.addUserRank(gamerId, group, expires)
                .thenAccept(this::addRankAndUpdate);
    }

    @Override
    public void addRank(@NonNull UserRank rank) {
        ((UserRanks) ranks).add(rank);

        val event = new PlayerRankAddEvent(this, rank);
        MegamindApp.getInstance().getPluginManager().callEvent(event);
    }

    @Override
    public void setRank(@NonNull Group group, final LocalDateTime expires) {
        RankSqlHandler.INSTANCE.setRank(gamerId, group, expires).thenAccept(this::setRankAndUpdate);
    }

    public void setRank(UserRank rank) {
        ranks.clear();
        ((UserRanks) ranks).add(rank);

        val event = new RankChangedEvent(this, rank);
        MegamindApp.getInstance().getPluginManager().callEvent(event);
    }

    public void addRankAndUpdate(final @NonNull UserRank rank) {
        addRank(rank);

        Packet<?> updateRank = new B101UpdateRank(gamerId, B101UpdateRank.Action.ADD_RANK,
                rank.getGroup().getId(), rank.getId(), (int) rank.getExpires(TimeUnit.SECONDS));

        sendToServer(updateRank);
        sendToProxy(updateRank);
    }

    public void setRankAndUpdate(final @NonNull UserRank rank) {
        setRank(rank);

        Packet<?> updateRank = new B101UpdateRank(gamerId, B101UpdateRank.Action.SET_RANK,
                rank.getGroup().getId(), rank.getId(), (int) rank.getExpires(TimeUnit.SECONDS));

        sendToServer(updateRank);
        sendToProxy(updateRank);
    }

    public void removeRank(final @NonNull UserRank rank) {
        ranks.remove(rank);

        RankSqlHandler.INSTANCE.removeUserRank(rank.getId());

        val event = new PlayerRankRemoveEvent(this, rank);
        MegamindApp.getInstance().getPluginManager().callEvent(event);
    }

    public void removeRankAndUpdate(final @NonNull UserRank rank) {
        removeRank(rank);

        val updateRank = new B101UpdateRank(gamerId, B101UpdateRank.Action.REMOVE_RANK,
                rank.getGroup().getId(), rank.getId(), (int) rank.getExpires(TimeUnit.SECONDS));

        sendToServer(updateRank);
        sendToProxy(updateRank);
    }

    public synchronized ConnectStatus sendTo(final @NonNull AbstractPlayerHolder server) {
        // any bukkit/virtualized instance, but not a PROXY lol
        assert server instanceof IPlayableBukkitServer;

        if (!server.canJoin()) {
            sendLocalizedMessage(isModern(), "server_loading");
            return ConnectStatus.SERVER_LOADING;
        }

        if (getCurrentServer() == server) {
            sendLocalizedMessage(isModern(), "server_already_connected");
            return ConnectStatus.ALREADY_CONNECTED;
        }

/*        if (server instanceof GameServer && isVanished() && !ranks.canApply(Group.HELPER)) {
            sendLocalizedMessage(isModern(), "core_vanish_noserver");
            return ConnectStatus.REJECTED_BY_DESTINATION; // todo: write different state enum
        }*/

        if (server instanceof GameServer && getCurrentServer() instanceof GameServer) {
            val oldGame = (GameServer) getCurrentServer();
            val newGame = (GameServer) server;
            if (oldGame.getParentServer().getName().equals(newGame.getParentServer().getName())) {
                sendToServer(new S26UpdatePlayerGameServer(gamerId, newGame.getGameId()));
            }
        }

        this.currentlyConnectingTo = (IPlayableBukkitServer) server;

        sendToProxy(new S18SendPlayer(gamerId, currentlyConnectingTo.getName()));

        sendLocalizedMessage(isModern(), "server_teleportation_started", "%server%",
                server.getName());

        return ConnectStatus.SUCCESS;
    }

    // endregion

    public AbstractPlayerHolder getCurrentServer() {
        if (gameServer != null) return gameServer;
        if (lobbyServer != null) return lobbyServer;
        if (persistentServer != null) return persistentServer;
        return bukkitServer;
    }

    @Override
    public void setSetting(final @NonNull SettingType settingType,
                           final @NonNull Setting<?, ?> value) {

        updateSetting(settingType, value);
        sendSettingChange(settingType, value);
    }

    public void updateSetting(final @NonNull SettingType settingType,
                              final @NonNull Setting<?, ?> value) {
        if (gamerId < 0 || settings == null || settings.isEmpty()) {
            return;
        }

        Logging.IMP.info("REPLACE CALLED {} -> {}", settingType, value);
        settings.replace(settingType, value);

        SettingsSqlHandler.INSTANCE.updateSettings(gamerId,
                settings);
    }

    private void sendSettingChange(final @NonNull SettingType settingType,
                                   final @NonNull Setting<?, ?> value) {
        val packet = new B109UpdateSetting(gamerId, settingType.name(), String.valueOf(value.serialize()));
        sendToServer(packet);
        sendToProxy(packet);
    }

    public int getFriendLimit() {
        return 100;
    }

    @Override
    public void addProperty(@NonNull String property, int value) {
        int oldValue = getPropertyOrDefault(property, 0);
        int newValue = oldValue + value;

        updateProperty(property, newValue);
        sendPropertyChange(property, newValue);
    }

    @Override
    public <T> void setProperty(final @NonNull String property,
                            final T value) {
        updateProperty(property, value);
        sendPropertyChange(property, value);
    }

    private <T> void sendPropertyChange(final @NonNull String property,
                                    final T value) {
        PropertyType<T> propertyType = (PropertyType<T>) properties.get(property);
        propertyType.setValue(value);
        Packet<?> packet = new B102SetProperty(gamerId, property, propertyType);
        sendToServer(packet);
        sendToProxy(packet);
    }

    public <T> void updateProperty(final @NonNull String key,
                               final T value) {
        if (gamerId < 0 || properties == null) {
            return;
        }

        PropertyType<T> propertyType = (PropertyType<T>) properties.get(key);

        if (propertyType == null) {
            propertyType = PropertySqlHandler.INSTANCE.createProperty(value);
        }

        propertyType.setValue(value);


        Logging.IMP.info("UPDATE PROPERTY {} -> {}", key, value);
        Logging.IMP.info(propertyType.getInsertStatement(1, "test"));

        properties.put(key, propertyType);
        PropertySqlHandler.INSTANCE.setPropertyValue(gamerId, key, propertyType).join();
    }

    @Override
    public void removeProperty(final @NonNull String key) {
        if (gamerId < 0 || properties == null) {
            return;
        }

        dropProperty(key);

        B103RemoveProperty packet = new B103RemoveProperty(gamerId, key);
        sendToProxy(packet);
        sendToServer(packet);
    }

    @Override
    public void dropProperty(final @NonNull String key) {
        if (gamerId < 0 || properties == null) {
            return;
        }

        properties.remove(key);
        PropertySqlHandler.INSTANCE.removeProperty(gamerId, key).join();
    }

    // endregion

    /**
     * Basically sends a packet to proxy that player is connected to.
     */
    @Override
    public void sendToProxy(final @NonNull Packet<?> packet) {
        if (online && proxyServer != null) {
            proxyServer.getChannel().sendPacket(packet);
        }
    }

    /**
     * Sends a packet to the server the player is connected to
     */
    @Override
    public void sendToServer(final @NonNull Packet<?> packet) {
        if (online && bukkitServer != null) {
            bukkitServer.getChannel().sendPacket(packet);
        }
    }

    /**
     * Called when a player disconnects from proxy
     */
    @Override
    public void quit() {
        this.online = false;

        // TODO: Update tab, playtime etc.
        updateServer(null);

        MegamindApp.INSTANCE.pluginManager().callEvent(new GamerQuitEvent(this));
        Gamer.removeGamer(this);
    }

    @Override
    public void load() {
        super.load();
        this.loadAuth();
        // todo: add more ...
    }

    @Override
    public void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut) {
        sendToProxy(new S13SendTitle(gamerId, title, subtitle, fadeIn, stay, fadeOut));
    }

    @Override
    public void disconnect(@NonNull String reason) {
        sendToProxy(new B107ProxyKick(gamerId, reason));
    }


    @Override
    public void sendRawMessage(String message) {
        sendToProxy(new B106Chat(gamerId, message, true));
    }

    @Override
    public void sendLocalizedMessage(boolean modern, @NonNull String key, Object... objects) {
        super.sendLocalizedMessage(modern, key, objects);
    }

    @Override
    public @NotNull String getLowerName() {
        return super.getLowerName();
    }

    @Override
    public void sendMessage(@NonNull String message) {
        sendToProxy(new B106Chat(gamerId, message, false));
    }

    private void loadAuth() {
        if (gamerId == -1) {
            return;
        }

        registered = AuthSqlHandler.INSTANCE.exists(gamerId).join();
    }


    // region vanish
    public boolean setVanished(final boolean vanished) {
        boolean oldValue = isVanished();

        // nothing changed
        if (oldValue == vanished) {
            return false;
        }

        // all shit validated, so we send vanish packet to server
        setProperty("vanished", vanished);

        val server = getCurrentServer();

        if (server != null) {
            LookupManager.INSTANCE.handleOnlineChange((AbstractScalable) server);
        }

        return true;
    }

    public boolean setBuildMode(final boolean buildMode) {
        boolean oldValue = hasBuildMode();

        // nothing changed
        if (oldValue == buildMode) {
            return false;
        }

        // all shit validated, so we send vanish packet to server
        setProperty("build_mode", buildMode ? 1 : 0);

        val server = getCurrentServer();

        if (server != null) {
            LookupManager.INSTANCE.handleOnlineChange((AbstractScalable) server);
        }

        return true;
    }

    public void sendLocalizedMessage(@NonNull String key, Object... objects) {
        super.sendLocalizedMessage(isModern(), key, objects);
    }

    public boolean canSeeVanish() {
        return false;
    }

    // region data loaders

    // relay
    public static Gamer getGamer(final @NonNull String name) {
        return CommonGamer.getGamer(name);
    }

    // non-relay
    public static Gamer getOfflineGamer(final @NonNull String name,
                                        final @Nullable Consumer<Gamer> consumer) {
        val onlineGamer = getGamer(name);

        if (onlineGamer != null) {
            return onlineGamer;
        }

        var offlineGamer = getOfflineGamer(name);

        boolean isNull = offlineGamer == null;

        if (isNull) {
            offlineGamer = new Gamer(name);
            offlineGamer.loadPlayerId();

            if (offlineGamer.getGamerId() < 0) {
                return null;
            }

            // loading name in normal case
            offlineGamer.loadPlayerName();

            synchronized (OFFLINE_GAMERS) {
                OFFLINE_GAMERS.store(offlineGamer);
            }
        }

        // invoking loader
        if (consumer != null) consumer.accept(offlineGamer);

        if (isNull) {
            MegamindApp.INSTANCE.pluginManager().callEvent(new OfflineGamerLoadedEvent(offlineGamer));
        }

        return offlineGamer;
    }

    // non-relay
    public static Gamer getOfflineGamer(final int playerId,
                                        final @NonNull Consumer<Gamer> loader) {
        val onlineGamer = getGamer(playerId);

        if (onlineGamer != null) {
            return onlineGamer;
        }

        val uuid = NetworkSqlHandler.INSTANCE.getUniqueId(playerId);

        if (uuid == null) {
            return null;
        }

        val offlineGamer = new Gamer(uuid);
        offlineGamer.setGamerId(playerId);

        // loading name in normal case
        offlineGamer.loadPlayerName();

        synchronized (OFFLINE_GAMERS) {
            OFFLINE_GAMERS.store(offlineGamer);
        }

        loader.accept(offlineGamer);

        return offlineGamer;
    }

    // relay
    public static Gamer getOfflineGamer(final @NonNull String name) {
        return CommonGamer.getOfflineGamer(name);
    }

    // relay
    public static Gamer getOfflineGamer(final @NonNull UUID uuid) {
        return CommonGamer.getOfflineGamer(uuid);
    }

    // relay
    public static Gamer getOfflineGamer(final int playerId) {
        return CommonGamer.getOfflineGamer(playerId);
    }

    // relay
    public static Gamer getGamer(final int playerId) {
        return CommonGamer.getGamer(playerId);
    }

    // relay
    public static void kickAll() {
        CommonGamer.kickAll();
    }

    public static Collection<Gamer> getGamers() {
        return CommonGamer.getGamers();
    }

    public static List<IGamer> getGamersByIp(final @NonNull InetAddress address) {
        synchronized (GAMERS) {
            return GAMERS.values().stream()
                    .filter(player -> ((CommonGamer) player).getAddress().equals(address))
                    .collect(Collectors.toList());
        }
    }

    public List<Gamer> getOnlineFriends() {
        if (friends == null) {
            return Collections.emptyList();
        }

        return IntStream.of(friends.stream().mapToInt(f -> f.getFriendIdFor(gamerId)).toArray())
                .mapToObj(Gamer::getGamer)
                .filter(gamer -> gamer != null && gamer.getCurrentServer() != null)
                .collect(Collectors.toList());
    }

    public static List<IGamer> getGamersByGroup(final @NonNull Group group) {
        synchronized (GAMERS) {
            return GAMERS.values().stream()
                    .filter(player -> player.getRanks().canApply(group))
                    .collect(Collectors.toList());
        }
    }

    // relay
    public static void addGamer(final @NonNull Gamer gamer) {
        Logging.IMP.info("[DEBUG] Added {}/{}", gamer.getName(), gamer.getGamerId());
        CommonGamer.addGamer(gamer);
        LookupManager.INSTANCE.handleGlobalOnlineChange(getTotalOnline());
    }

    // non-relay
    public static void removeGamer(final @NonNull Gamer gamer) {
        gamer.updateServer(null);

        if (gamer.getProxyServer() != null) {
            gamer.getProxyServer().removePlayer(gamer);
            gamer.setProxyServer(null);
        }

        synchronized (OFFLINE_GAMERS) {
            OFFLINE_GAMERS.remove(gamer);
        }

        synchronized (GAMERS) {
            GAMERS.remove(gamer);
        }

        LookupManager.INSTANCE.handleGlobalOnlineChange(getTotalOnline());
        Logging.IMP.info("[DEBUG] Removed {}/{}", gamer.getName(), gamer.getGamerId());
    }

    // relay
    public static void invalidateOfflinePlayer(final @NonNull Gamer gamer) {
        CommonGamer.invalidateOfflinePlayer(gamer);
    }

    // relay
    public static int getTotalOnline() {
        return CommonGamer.getTotalOnline();
    }

    @Override
    public void dispatchCommand(@NonNull String command) {
        CommandSender.super.dispatchCommand(command);
    }

    @Override
    public void sendCommandUsageMessage(@NonNull String usageKey) {
        CommandSender.super.sendCommandUsageMessage(usageKey);
    }

    @Override
    public UserRanks getRanks() {
        return (UserRanks) ranks;
    }

    // endregion
}
