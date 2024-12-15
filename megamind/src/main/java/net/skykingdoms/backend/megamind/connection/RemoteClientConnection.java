package net.skykingdoms.backend.megamind.connection;

import io.netty.channel.socket.SocketChannel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.backend.setting.impl.BooleanSetting;
import net.skykingdoms.api.backend.setting.impl.EnumSetting;
import net.skykingdoms.api.megamind.event.impl.gamer.*;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.api.protocol.type.ReportType;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.data.sql.sql.MapSqlHandler;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.ConsoleCommandSender;
import net.skykingdoms.backend.megamind.data.auth.AuthManager;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.data.punishment.PunishmentManager;
import net.skykingdoms.backend.megamind.event.GetBestServerEvent;
import net.skykingdoms.backend.megamind.event.PlayerRedirectEvent;
import net.skykingdoms.backend.megamind.event.core.ClientActiveEvent;
import net.skykingdoms.backend.megamind.lookup.LookupManager;
import net.skykingdoms.backend.megamind.manager.*;
import net.skykingdoms.backend.megamind.manager.deployment.DeploymentResponse;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.megamind.server.impl.*;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteClientChannel;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.PacketProtocol;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import net.skykingdoms.backend.protocol.packet.play.*;
import net.skykingdoms.backend.protocol.server.IConnectable;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import net.skykingdoms.backend.protocol.type.GameBootResponse;
import net.skykingdoms.backend.protocol.type.connect.ConnectStatus;
import net.skykingdoms.backend.protocol.type.reconnect.FailedReconnectGamer;

import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RemoteClientConnection extends AbstractRemoteClientChannel {
    @Getter final String host;

    @Getter final int port;

    @Setter @Getter
    IConnectable connectable;

    public RemoteClientConnection(final @NonNull SocketChannel channel) {
        super(channel);
        
        this.host = channel.remoteAddress().getHostString();
        this.port = channel.remoteAddress().getPort();
    }

    private String getDisplayName() {
        return connectable == null ? channel.remoteAddress().toString() : connectable.getName();
    }

    @Override
    public void active() {
        MegamindApp.INSTANCE.pluginManager().callEvent(new ClientActiveEvent(this));
    }

    @Override
    protected void onDisconnect() {
        if (connectable instanceof ServiceServer) {
            val ss = (ServiceServer) connectable;
            ServiceManager.INSTANCE.removeService(ss);
        }

        if (connectable instanceof NodeServer) {
            val ns = (NodeServer) connectable;
            NodeManager.INSTANCE.removeNode(ns);
        }

        if (connectable instanceof ProxyServer) {
            val ps = (ProxyServer) connectable;
            ProxyManager.INSTANCE.removeProxyServer(ps.getName());
        }

        if (connectable instanceof BukkitServer) {
       /*     Logging.IMP.info("IT'S A BUKKIT SERVER");*/
            val bs = (BukkitServer) connectable;
            ServerManager.INSTANCE.removeBukkitServer(bs.getName(), bs.getScaleType() != ServerScaleType.PERSISTENT, false);
        }

        if (connectable instanceof PersistentServer) {
            val ps = (PersistentServer) connectable;
            ServerManager.INSTANCE.removePersistentServer(ps);
        }

        Logging.IMP.info("[RCC/DEBUG] Disconnected {}:{}:{}", getDisplayName(), host, port);
    }

    @Override
    public void process(SecurityToken.Request packet, ChannelProcessorContext ctx) {
        if (!CoreSecurityManager.INSTANCE.isInitialized()) {
            Logging.IMP.error("[{}] [Security] Requested token, but security manager was not initialized!");
            ctx.setResponse(new SecurityToken.Response("")); // sending blank. it will be rejected on endpoint
            return;
        }

        ctx.setResponse(new SecurityToken.Response(CoreSecurityManager.INSTANCE.getBungeeSecurityToken()));
    }

    @Override
    public void process(ProxyPlayerLogin.Request packet, ChannelProcessorContext ctx) {
        val name = packet.getName().toLowerCase();
        val uuid = packet.getUuid();
        val gamer = Gamer.getGamer(name);

        // FIXME
        synchronized (PENDING_LOGINS) {
            if ((gamer != null && gamer.isOnline()) ||
                    (!packet.contains(ProxyPlayerLogin.FINISH) && !PENDING_LOGINS.add(uuid))) {
                ctx.setResponse(new ProxyPlayerLogin.Response(true, false, "§cYou are already connected to the server"));
                return;
            }
        }

        ctx.addLatch();

        try {
            asyncProxyPlayerLogin(packet, ctx);
        } finally {
            ctx.removeLatch();

            if (packet.contains(ProxyPlayerLogin.FINISH)) {
                PENDING_LOGINS.remove(uuid);
            } else if (PENDING_LOGINS.contains(uuid)) {
                // на случай если прокся не пришлет нам FINISH
                channel.eventLoop().schedule(() -> {
                    PENDING_LOGINS.remove(uuid);
                }, 3, TimeUnit.SECONDS);
            }
        }

    }

    private void asyncProxyPlayerLogin(ProxyPlayerLogin.Request packet, ChannelProcessorContext ctx) {
        boolean cancelled = false;
        String reason = null;
        boolean sessionLogin = false;

        try {
            val uuid = packet.getUuid();
            val name = packet.getName();
            val address = packet.getAddress();

            if (packet.contains(ProxyPlayerLogin.FINISH)) {
                val playerLoginEvent = callLoginEvent(name, uuid, address, PlayerLoginEvent.State.POST);

                if (playerLoginEvent.isCancelled()) {
                    cancelled = true;
                    reason = playerLoginEvent.getKickReason();

                    return;
                }

                val proxy = (ProxyServer) connectable;

                val gamer = new Gamer(uuid);
                gamer.setAddress(address);
                gamer.setOnline(true);
                gamer.loadPlayerId();

                if (!gamer.hasGamerId()) {
                    cancelled = true;
                    reason = "§cData is not loaded [PL/NF]";

                    return;
                }

                val activePunishment = PunishmentManager.INSTANCE.getActivePunishment(gamer.getGamerId(), PunishmentType.BAN);
                if (activePunishment != null) {
                    cancelled = true;
                    // we format ban by default language
                    reason = PunishmentManager.INSTANCE.getPunishmentReasonFormatted(activePunishment, Language.DEFAULT);
                    return;
                }

                gamer.load();

                gamer.setDomain(packet.getDomain());
                gamer.setProtocol(packet.getProtocol());
                gamer.setName(packet.getName());
                gamer.setPremium(packet.isPremium());
                gamer.setLoggedIn(packet.isPremium());

                // Listening and loading all the connection in GamerLoadEvent
                val event = new GamerLoadEvent(gamer);
                MegamindApp.INSTANCE.pluginManager().callEvent(event);

                val offline = Gamer.getOfflineGamer(uuid);

                if (offline != null) {
                    UUID session = offline.getSession();
                    if (!packet.isPremium() && session != null) {
                        if (AuthManager.INSTANCE.sessionMatches(address.getHostAddress(), session)) {
                            sessionLogin = true;
                            gamer.setLoggedIn(true);
                        }
                    }
                    Gamer.invalidateOfflinePlayer(offline);
                }

                if (!gamer.isLoggedIn()) {
                    AuthManager.INSTANCE.addAuthenticator(gamer);
                }


                val gple = MegamindApp.INSTANCE.pluginManager().callEvent(new GamerProxyLoginEvent(gamer, false));
                if (gple.isCancelled()) {
                    cancelled = true;
                    reason = gple.getKickReason();
                    return;
                }

                Gamer.addGamer(gamer);

                proxy.addPlayer(gamer);
                gamer.setProxyServer(proxy);
            }
        } finally {
            ctx.setResponse(new ProxyPlayerLogin.Response(cancelled, sessionLogin, reason));
        }
    }

    @Override
    public void process(C17LookupGlobalOnline packet, ChannelProcessorContext ctx) {
        if (connectable == null) {
            Logging.IMP.warn("[RCC] Tried registering online lookup, but remote client is null [{}]", packet);
            return;
        }

        LookupManager.INSTANCE.addOnlineLookup(connectable);
    }

    @Override
    public void process(C0PlayerConnected packet, ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

/*        Logging.IMP.info("I AM HERE.");*/

        if (gamer == null) {
            return;
        }

        MegamindApp.INSTANCE.networkState().getOnJoin().accept(gamer);

        // gamer just joined so we call GamerJoinEvent
        val oldJjFlag = gamer.isJustJoined();
        if (gamer.isJustJoined()) {

            if (Gamer.getGamersByIp(gamer.getAddress()).size() > 4) {
                gamer.disconnect("§cToo many accounts online at once, max allowed is 4!");
            }

            MegamindApp.INSTANCE.pluginManager().callEvent(new GamerJoinEvent(gamer));
            gamer.setJustJoined(false);
        }

        val server = (AbstractPlayerHolder) ServerManager.INSTANCE.getPlayableServer(packet.getServer());
        if (server == null) {
            // todo: fixme
            Logging.IMP.warn("[{}] Kicking, due to moving to absent server {}", packet.getServer());
            gamer.disconnect("Internal server error");
            return;
        }

        Logging.IMP.info("[{} -> {}] JJ: {}", gamer.getName(), server.getName(), oldJjFlag);

        gamer.updateServer((AbstractScalable) server);
        gamer.loadFriends();

/*        for (Gamer friend : gamer.getOnlineFriends()) {
            friend.sendLocalizedMessage("friend_join_the_game", "%name%", gamer.getColoredName());
        }*/
    }


    private PlayerLoginEvent callLoginEvent(final @NonNull String name,
                                            final @NonNull UUID uuid,
                                            final @NonNull InetAddress address,
                                            final @NonNull PlayerLoginEvent.State state) {
        return MegamindApp.INSTANCE.pluginManager().callEvent(new PlayerLoginEvent(name, uuid, address, state));
    }

/*    @Override
    public void process(C12ProxyPlayerDisconnect packet, ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer != null) {
            gamer.quit();
        }

        Logging.IMP.info("[{}] Disconnected {}", getDisplayName(), gamer == null ? "N/A" : gamer.getName());
    }*/

    @Override
    public void process(ServerConnect.Request packet, ChannelProcessorContext ctx) {
        String serverName = packet.getTarget();
        boolean cancelled = false;

        try {
            val gamer = Gamer.getGamer(packet.getPlayerId());

            if (gamer == null) {
                return;
            }

            val target = (AbstractPlayerHolder) ServerManager.INSTANCE.getPlayableServer(serverName);

            if (target == null) {
                val finalMom = serverName;
                serverName = ServerManager.INSTANCE.getFallbackServer().getName();
                gamer.sendMessage("Couldn't connect you to " + finalMom + " so you were routed to fallback server (" + serverName + ")");
                return;
            } else {
                serverName = target.getName();
            }

            val event = new PlayerRedirectEvent(gamer, target);
            MegamindApp.INSTANCE.pluginManager().callEvent(event);

            cancelled = event.isCancelled();
            serverName = event.getTarget().getName();
        } finally {
            ctx.setResponse(new ServerConnect.Response(cancelled, serverName));
        }
    }

    public static final Set<UUID> PENDING_LOGINS = new HashSet<>();


    @Override
    public void process(C10ReconnectPlayers packet, ChannelProcessorContext ctx) {
        ctx.addLatch();

        AsyncExecution.INSTANCE.run(() -> {
            asyncReconnectPlayers(packet);
            ctx.removeLatch();
        });
    }

    private void asyncReconnectPlayers(C10ReconnectPlayers packet) {
        val proxy = (ProxyServer) connectable;

        Logging.IMP.info("Reconnecting " + packet.getGamers().size() + " gamers");

        Set<FailedReconnectGamer> gamers = new HashSet<>();
        Map<Gamer, CompletableFuture<AbstractPlayerHolder>> reconnecting = new HashMap<>();

        for (val reconnectingGamer : packet.getGamers()) {
            val name = reconnectingGamer.getName();
            val uuid = reconnectingGamer.getUuid();
            val address = reconnectingGamer.getAddress();
            val serverName = reconnectingGamer.getServer();

            val target = Gamer.getGamer(name);

            if (target != null && target.isOnline()) {
                gamers.add(new FailedReconnectGamer(name, uuid, "§cYou are already connected to the server"));
                continue;
            }

            val playerLoginEvent = callLoginEvent(name, uuid, address, PlayerLoginEvent.State.RECONNECT);

            if (playerLoginEvent.getKickReason() != null) {
                gamers.add(new FailedReconnectGamer(name, uuid, playerLoginEvent.getKickReason()));
                continue;
            }

            val gamer = new Gamer(reconnectingGamer.getUuid());
            gamer.setAddress(reconnectingGamer.getAddress());
            gamer.setName(reconnectingGamer.getName());
            gamer.loadPlayerId();

            if (gamer.getGamerId() < 0) {
                gamers.add(new FailedReconnectGamer(name, uuid, "§cThis player does not exist"));
                continue;
            }

            gamer.setDomain(reconnectingGamer.getDomain());
            gamer.setProtocol(reconnectingGamer.getProtocol());
            gamer.setProxyServer(proxy);
            gamer.setLoggedIn(reconnectingGamer.isLoggedIn());
            gamer.load();

            // we already had this player connected
            gamer.setJustJoined(false);

            proxy.addPlayer(gamer);

            // Listening and loading all the connection in GamerLoadEvent
            val event = new GamerLoadEvent(gamer);
            MegamindApp.INSTANCE.pluginManager().callEvent(event);

            Gamer.addGamer(gamer);

            val gamerProxyLoginEvent = new GamerProxyLoginEvent(gamer, true);
            MegamindApp.INSTANCE.pluginManager().callEvent(gamerProxyLoginEvent);

            try {
                reconnecting.put(
                        gamer,
                        ServerManager.INSTANCE.getFutureServer(serverName).whenComplete((result, e) -> {
                            if (result == null || e != null) {
                                gamer.localizedDisconnect("core_reconnect_server_not_found");
                            } else {
                                gamer.setOnline(true);

                                gamer.updateServer((AbstractScalable) result);
                                Logging.IMP.info("Reconnected {} -> {}", gamer.getName(), result.getName());

                                gamer.sendLocalizedMessage(gamer.isModern(), "core_reconnect_success_title");
                                gamer.sendLocalizedMessage(gamer.isModern(), "core_reconnect_success_subtitle");
                            }
                        })
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            MegamindApp.INSTANCE.pluginManager().callEvent(new GamerJoinEvent(gamer));
        }

        if (!gamers.isEmpty()) {
            sendPacket(new S12FailedReconnectPlayers(gamers));
        }

        if (!reconnecting.isEmpty()) {
            for (val server : reconnecting.values()) {
                if (server.isDone()) {
                    continue;
                }

                try {
                    server.get(10000, TimeUnit.MILLISECONDS);
                } catch (Throwable t) {
                    server.completeExceptionally(t);
                }
            }
        }
    }

    @Override
    public void process(final @NonNull Handshake.Request packet,
                        final @NonNull ChannelProcessorContext ctx) {
        if (!packet.getSecret().equals(MegamindApp.INSTANCE.secret())) {
            ctx.setResponse(new Handshake.Response(Handshake.Result.FAILED, "Invalid secret token!"));
            Logging.IMP.info("[HS/D] Aborted handshake request of {} due to invalid secret key ({}), should be ({})!",
                    getDisplayName(), packet.getSecret(), MegamindApp.INSTANCE.secret());
            close();
            return;
        }

        if(!DeploymentManager.INSTANCE.validateConnection(packet)) {
            Logging.IMP.info("[HS/D] Rejected handshake of {}", getDisplayName());
            ctx.setResponse(new Handshake.Response(Handshake.Result.FAILED, "Invalid remote client connection"));
            close();
            return;
        }

        sendPacket(new Handshake.Response(Handshake.Result.SUCCESS, null));
        upgradeConnection(PacketProtocol.PLAY);
        ctx.done();

        DeploymentManager.INSTANCE.handleRemoteConnection(
                this,
                packet
        );

        Logging.IMP.info("[HS/D] Accepted handshake of {}", getDisplayName());
    }

    @Override
    public void process(NetworkPlayerLoad.Request packet, ChannelProcessorContext ctx) {
        ctx.setResponse(new NetworkPlayerLoad.Response(getPlayers(packet.getPlayerIds())));
        Logging.IMP.info("[{}] Requested network player load! IDs: {}",
                getDisplayName(),
                getPlayers(packet.getPlayerIds()).stream().map(holder -> holder.getPlayerId() + "/" + holder.getServer()).collect(Collectors.joining(", ")));
    }

    public List<NetworkPlayerLoad.PlayerHolder> getPlayers(int[] playerIds) {
        List<NetworkPlayerLoad.PlayerHolder> result = new ArrayList<>(playerIds.length);

        for (val playerId : playerIds) {
            val gamer = Gamer.getGamer(playerId);
            NetworkPlayerLoad.PlayerHolder player;

            if (gamer == null) {
                player = new NetworkPlayerLoad.PlayerHolder(playerId);
            } else {

                // ????
                String serverName = gamer.getCurrentlyConnectingTo() == null
                        ? "NULL"
                        : gamer.getCurrentlyConnectingTo().getName();
                player = new NetworkPlayerLoad.PlayerHolder(
                        true, playerId, gamer.getProtocol(), gamer.isLoggedIn(), serverName
                );
            }

            result.add(player);
        }

        return result;
    }

    @Override
    public void process(C16RegisterLookups packet, ChannelProcessorContext ctx) {
        if (connectable instanceof AbstractScalable) {
            Logging.IMP.info("[LookupsManager] Registered {} lookups for {} -> {}", packet.getTargets().length,
                    connectable.getName(),
                    Arrays.stream(packet.getTargets())
                            .map(lookupTarget -> "\n- " + lookupTarget.getData().toString() + ": " + Arrays.stream(lookupTarget.getFlags())
                                    .map(Enum::name)
                                    .collect(Collectors.joining(", "))
                            )
                            .collect(Collectors.joining("\n")));

            LookupManager.INSTANCE.addLookup((AbstractScalable) connectable, packet.getTargets());
        }
    }

    @Override
    public void process(C15GetBestServer packet, ChannelProcessorContext ctx) {
        val event = new GetBestServerEvent(packet.getName(), packet.getAddress());

        MegamindApp.INSTANCE.pluginManager().callEvent(event);

        val server = event.getBestServer() == null
                ? ServerManager.INSTANCE.getBestByOnline(Gamer.getGamer(packet.getName()), packet.getData(), true)
                : event.getBestServer();

        ctx.setResponse(new S19BestServer(server == null ? null : server.getName()));
    }

    @Override
    public void process(LobbyBoot.Request packet, ChannelProcessorContext ctx) {
        if (!(connectable instanceof BukkitServer)
                || ((BukkitServer) connectable).getScaleType() != ServerScaleType.LOBBY) {
            Logging.IMP.error("Received lobby deploy packet from server with invalid scaling!");
            return;
        }

        DeploymentManager.INSTANCE.handleLobbyBoot(packet, this, ctx);
    }

    @Override
    public void process(C12ProxyPlayerDisconnect packet, ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer != null) {
/*            for (Gamer friend : gamer.getOnlineFriends()) {
                friend.sendLocalizedMessage("friend_left_the_game", "%name%", gamer.getColoredName());
            }*/
            gamer.quit();
        }

        Logging.IMP.info("[{}] Disconnected {}", getDisplayName(), gamer == null ? "N/A" : gamer.getName());
    }

    @Override
    public void process(RequestMap.Request packet, ChannelProcessorContext ctx) {
        val miniName = getConnectable().getName() + ((char) ('A' + packet.getArenaId() - 1));
        Logging.IMP.info("[{}] Requested maps by {}", getDisplayName(), miniName);
        val bukkitServer = (BukkitServer) getConnectable();

        Logging.IMP.info("[{}] Requested testflow map for {}", getDisplayName(), miniName);

        val testflowMap = TestFlowManager.INSTANCE.getTestflowMap((AbstractScalable) getConnectable());

        Logging.IMP.info("[{}] TestFlow map for {} is supposedly loaded", getDisplayName(), miniName);
        if (testflowMap != null) {
            val executor = TestFlowManager.INSTANCE.getActiveTestFlow(bukkitServer.getLinkedTestFlowId()).getExecutor();

            Logging.IMP.info("[{}] TestFlow #{} has the forced map -> {}", getDisplayName(), bukkitServer.getLinkedTestFlowId(), testflowMap);
            if (executor != null) {
                executor.sendMessage(ChatColor.GREEN + "Instance " + miniName + " of " + GameType.getLinkedGameType(packet.getGameMode()).getHumanName() + " " + packet.getGameMode().getHumanName() + " game is locked at map " + testflowMap);
            }

            ctx.setResponse(new RequestMap.Response(testflowMap));
            return;
        }

        Logging.IMP.info("[{}] Starting process for {}", getDisplayName(), miniName);
        val available = MapSqlHandler.INSTANCE.getMaps(packet.getGameMode());
        Logging.IMP.info("[{}] Available {} for {}", getDisplayName(), available, miniName);
        val index = packet.getArenaId() % available.size();
        Logging.IMP.info("[{}] Sending response for {}", getDisplayName(), miniName);
        ctx.setResponse(new RequestMap.Response(available.get(index).getFirst()));
        Logging.IMP.info("[{}] Sent response for {}", getDisplayName(), miniName);
    }

    @Override
    public void process(final C18ConnectExact packet, ChannelProcessorContext ctx) {
        Gamer gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            ctx.setResponse(new S22ConnectResponse(ConnectStatus.GAMER_NOT_FOUND));
            return;
        }

        val server = ServerManager.INSTANCE.getPlayableServer(packet.getServer());

        if (server == null) {
            ctx.setResponse(new S22ConnectResponse(ConnectStatus.SERVER_NOT_FOUND));
            return;
        }

        ConnectStatus status = gamer.sendTo(server);

        if (status != null) {
            ctx.setStrictResponse(() -> new S22ConnectResponse(status));
        }
    }

    @Override
    public void process(C19LoadFriends packet, ChannelProcessorContext ctx) {
        // a bit of optimization
        // basically we send one packet with all players (friends)
        // instead of sending multiple packets requesting player data
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            ctx.setResponse(new NetworkPlayerLoad.Response(Collections.emptyList()));
            return;
        }

        ctx.setResponse(new NetworkPlayerLoad.Response(getPlayers(gamer.getFriends().stream().mapToInt(meta -> meta.getFriendIdFor(gamer.getGamerId())).toArray())));
    }

    @Override
    public void process(GameBoot.Request packet, ChannelProcessorContext ctx) {
        Logging.IMP.info("[{}] Requested game modes", getDisplayName());

        val tf = TestFlowManager.INSTANCE.getActiveTestFlow(((BukkitServer)connectable).getLinkedTestFlowId());
        if (tf != null) {
            val mode = tf.getData().getServerData().getGameMode();
            Logging.IMP.info("[{}] Testflow linked to server (#{}), so we send {}/{}", getDisplayName(), tf.getTestFlowId(), mode, tf.getAmount());
            ctx.setResponse(new GameBoot.Response(
                    new GameBootResponse[] {
                            new GameBootResponse(mode, tf.getAmount()),
                    }
            ));
            return;
        }

        if (ServerManager.INSTANCE.getPendingGameDeployments().containsKey(connectable.getName())) {
            val response = ServerManager.INSTANCE.getPendingGameDeployments().get(connectable.getName());
            Logging.IMP.info("[{}] Pending game deployments are present for this instance: ", Arrays.stream(response).map(m -> m.getGameMode().getHumanName() + "=" + m.getAmount()).collect(Collectors.joining(", ")));
            ctx.setResponse(new GameBoot.Response(response));
            ServerManager.INSTANCE.flushGameBoot(connectable.getName());
            return;
        }

/*
        // TODO: rewrite
        if (((BukkitServer) getConnectable()).getGameType() == GameType.SKYCLASH) {
            ctx.setResponse(new GameBoot.Response(
                    new GameBootResponse[]{
                            new GameBootResponse(GameMode.SURVIVAL_MODERN, 1)
                    }
            ));
        }
*/

        Logging.IMP.info("[{}] Sent shit: {}", getDisplayName(), Arrays.stream(((GameBoot.Response) ctx.getResponse()).getDataArray()).map(s -> s.getGameMode().name() + "/" + s.getAmount()).collect(Collectors.joining(", ")));
    }

    @Override
    public void process(B106Chat packet, ChannelProcessorContext ctx) {
        Gamer gamer = Gamer.getGamer(packet.getReceiver());

        if (gamer == null) {
            return;
        }

        gamer.sendToProxy(new B106Chat(packet.getReceiver(), packet.getMessage(), packet.isJson()));
    }

    @Override
    public void process(Tabcomplete.Request packet, ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        val suggests = MegamindApp.INSTANCE.tabComplete(gamer, packet.getLine());

        if (suggests == null) {
            return;
        }

        ctx.setResponse(new Tabcomplete.Response(suggests));
    }


    @Override
    public void process(C07ServerRegisterCommands packet, ChannelProcessorContext ctx) {
        ServerManager.INSTANCE.getCommandMapper().registerCommands(connectable.getName(), packet.getCommands());
        ProxyManager.INSTANCE.sendToProxies(new S10RegisterCommands(S10RegisterCommands.CommandContainer.serverSingleton(packet.getCommands(), connectable.getName())));
    }

    @Override
    public void process(C20MessageChannelMessage packet, ChannelProcessorContext ctx) {
        int playerId = packet.getGamerId();
        int channelId = packet.getChannelId();
        String message = packet.getMessage();

        if (playerId == -1) {
            return;
        }

        Gamer gamer = Gamer.getGamer(playerId);

        if (gamer == null) {
            MegamindApp.INSTANCE.pluginManager().callEvent(new GamerMessageChannelEvent(null, channelId, message));
            return;
        }

        MegamindApp.INSTANCE.pluginManager().callEvent(new GamerMessageChannelEvent(gamer, channelId, message));
    }

    @Override
    public void process(OpenReportPacket.Request packet, ChannelProcessorContext ctx) {
        ReportType reportType = packet.getReportType();
        int senderId = packet.getSenderId();
        int targetId = packet.getTargetId();

        Gamer gamer = Gamer.getGamer(senderId);
        Gamer target = Gamer.getGamer(targetId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm").withZone(ZoneId.systemDefault());
        String formattedTime = formatter.format(Instant.now());

        StaffChatManager.INSTANCE.sendMessage(
                ChatColor.RED + "[" + formattedTime + "] " +
                        gamer.getColoredName() +
                        ChatColor.RED + " reported " +
                        target.getColoredName() +
                        ChatColor.RED + " for " +
                        Language.DEFAULT.getMessage(reportType.getLocalizationKey())
        );        ctx.setResponse(new OpenReportPacket.Response(true, null));
    }

    @Override
    public void process(B108UpdatePlayerLanguage packet, ChannelProcessorContext ctx) {
        int playerId = packet.getPlayerId();
        Gamer gamer = Gamer.getGamer(playerId);

        if (gamer == null) {
            return;
        }

        val language = Language.getById(packet.getLanguageId());

        gamer.setLanguage(language);

        NetworkSqlHandler.INSTANCE.updatePlayerLanguage(gamer.getGamerId(), language);

        if (connectable instanceof ProxyServer) {
            gamer.sendToServer(packet);
        } else {
            gamer.sendToProxy(packet);
        }

        gamer.updateTablist();
    }

    @Override
    public void process(B101UpdateRank packet, ChannelProcessorContext ctx) {
        Gamer gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        int groupId = packet.getGroupId();
        int rankId = packet.getRankId();
        int expires = packet.getExpires();

        val rank = new UserRank(rankId, Group.getById(groupId), expires);

        if (packet.getAction() == B101UpdateRank.Action.REMOVE_RANK) {
            gamer.removeRank(rank);
        } else if (packet.getAction() == B101UpdateRank.Action.ADD_RANK) {
            gamer.addRank(rank);
        } else if (packet.getAction() == B101UpdateRank.Action.SET_RANK) {
            gamer.setRank(rank);
        }

        if (connectable instanceof ProxyServer) {
            gamer.sendToServer(packet);
        } else {
            gamer.sendToProxy(packet);
        }
    }

    @Override
    public void process(B110UserUpdatePermission packet, ChannelProcessorContext ctx) {
        Gamer gamer = Gamer.getGamer(packet.getPlayerId());
        if (gamer != null) {
            gamer.sendToServer(packet);
        }
    }

    @Override
    public void process(B111GroupUpdatePermission packet, ChannelProcessorContext ctx) {
        GameMode gameMode = packet.getGameMode();

        if (gameMode == null) {
            ServerManager.INSTANCE.getServers().forEach(server -> server.getChannel().sendPacket(packet));
        } else {
            ServerManager.INSTANCE.getServers().stream()
                    .filter(server -> server.getGameMode() == gameMode)
                    .forEach(server -> server.getChannel().sendPacket(packet));
        }
    }

    @Override
    public void process(C09PlayerCommand packet, ChannelProcessorContext ctx) {
        Gamer gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer != null) {
            MegamindApp.INSTANCE.executeCommand(gamer, packet.getLine());
        }
    }

    @Override
    public void process(C36ServerCommand packet, ChannelProcessorContext ctx) {
        MegamindApp.INSTANCE.executeCommand(ConsoleCommandSender.INSTANCE, "/" + packet.getCommand());
    }

    @Override
    public void process(C08SendServerCommand packet, ChannelProcessorContext ctx) {
        boolean isExact = packet.isExact();
        String line = packet.getCommand();

        if (isExact) {
            String id = packet.getName();

            IConnectable remote = ServerManager.INSTANCE.getBukkitServer(id);

            if (remote == null) {
                remote = ProxyManager.INSTANCE.getProxy(id);
            }

            if (remote != null) {
                remote.getChannel().sendPacket(new S11ServerCommand(line));
            }
        } else {
            try {
                Logging.IMP.error("Not implemented sending pattern commands (yet)");
//                S11ServerCommand broadcastPacket = new S11ServerCommand(line);
//
//                for (val server : serverManager.findServers(packet.getName())) {
//                    server.sendPacket(broadcastPacket);
//                }
            } catch (Exception e) {
                Logging.IMP.error("Invalid pattern in {}", packet.toString());
            }
        }
    }

    @Override
    public void process(B109UpdateSetting packet, ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        val setting = SettingType.valueOf(packet.getSettingName(), null);
        if (setting == null) {
            Logging.IMP.error("Received setting update packet with invalid setting: {}", packet.getSettingName());
            return;
        }

        val settingValue = setting.getSetting();
        if (settingValue instanceof BooleanSetting) {
            val bs = (BooleanSetting) settingValue;
            bs.deserialize(Boolean.parseBoolean(packet.getSerializedValue()));
            gamer.updateSetting(setting, bs);
        }

        if (settingValue instanceof EnumSetting<?>) {
            @SuppressWarnings("rawtypes") val es = (EnumSetting) settingValue;
            es.deserialize(Integer.parseInt(packet.getSerializedValue()));
            gamer.updateSetting(setting, es);
        }

        if (connectable instanceof BukkitServer) {
            gamer.sendToProxy(packet);
        } else {
            gamer.sendToServer(packet);
        }
    }

    @Override
    public void process(B102SetProperty packet, ChannelProcessorContext ctx) {
        Gamer gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        gamer.updateProperty(packet.getKey(), packet.getProperty().getValue());

        if (connectable instanceof BukkitServer) {
            gamer.sendToProxy(packet);
        } else {
            gamer.sendToServer(packet);
        }
    }

    @Override
    public void process(B103RemoveProperty packet, ChannelProcessorContext ctx) {
        Gamer gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        gamer.dropProperty(packet.getProperty());

        if (connectable instanceof BukkitServer) {
            gamer.sendToProxy(packet);
        } else {
            gamer.sendToServer(packet);
        }
    }

    @Override
    public void process(C21RegisterGameInstance packet, ChannelProcessorContext ctx) {
        DeploymentManager.INSTANCE.handleGameCreate(packet, this);
        if (packet.getData().getGameType() == GameType.DUELS) {
            DuelManager.INSTANCE.serverDeployed(true);
        }
    }

    @Override
    public void process(C29RegisterPersistentInstance packet, ChannelProcessorContext ctx) {
        DeploymentManager.INSTANCE.handlePersistentCreate(packet, this);
    }

    @Override
    public void process(C22UpdateGameState packet, ChannelProcessorContext ctx) {
        ServerManager.INSTANCE.handleStateChange((BukkitServer) connectable, packet.getArenaId(), packet.getGameState());
    }

    @Override
    public void process(C31CreateDuel packet, ChannelProcessorContext ctx) {
        DuelManager.INSTANCE.addRequest(packet.getSenderId(), packet.getTargetId(), packet.getMode(), packet.getData());
    }

    @Override
    public void process(DuelData.Request packet, ChannelProcessorContext ctx) {
        val duel = DuelManager.INSTANCE.getRequest(packet.getGamerId());

        if (duel != null) {
            Logging.IMP.info("DuelData Response: " + duel.getMode().name());
            ctx.setResponse(new DuelData.Response(duel.getSenderId(), duel.getTargetId(), duel.getMode(), duel.getData()));
        }
    }

    @Override
    public void process(C32GetBestGameServerByMap packet, ChannelProcessorContext ctx) {
        // TODO: CALL MEGAMIND EVENT

        val server = ServerManager.INSTANCE.getBestGameServerByOnlineAndMap(Gamer.getGamer(packet.getName()), packet.getData(), packet.getMap(),true);

        if (server == null) {
            Logging.IMP.info("No server found for {} with map {}", packet.getName(), packet.getMap());
            Logging.IMP.info("Deploying new server for {} with map {}", packet.getName(), packet.getMap());

            GameBootResponse response = new GameBootResponse(packet.getData().getGameMode(), 1);

            if (ServerManager.INSTANCE.getBestByOnline(packet.getData().getGameType(), false) != null) {
                awaitPacket(new ArenaBoot.Request());
            }

            DeploymentManager.INSTANCE.sendDeploymentRequest(new BukkitDeployRequest(response.getGameMode().bakeData()),
                    ServerDeploy.DeploymentTarget.BUKKIT, false, null, -1,
                    (result) -> {
                        if (result == DeploymentResponse.DENIED_BY_NODE
                                || result == DeploymentResponse.DEPLOYMENT_TIMEOUT
                                || result == DeploymentResponse.TRANSACTION_FAILED) {
                            Logging.IMP.error("Transaction failed! (" + result.name() + ") / (#" + result.transactionId() + ")");
                        } else {
                            ServerManager.INSTANCE.queueGameInstanceBoot(result.transactionId(), new GameBootResponse[] {response});
                            Logging.IMP.info("Queued deployment of game instances!");
                        }
                    });

            //DeploymentManager.INSTANCE.sendAndWait
        }

        ctx.setResponse(new S35BestGameServerByMap(server == null ? null : server.getName()));
    }

    @Override
    public void process(C34GetBestDuelServer packet, ChannelProcessorContext ctx) {
        // TODO: CALL MEGAMIND EVENT

        ctx.addLatch();
        val server = ServerManager.INSTANCE.getBestGameServerByOnlineAndMap(Gamer.getGamer(packet.getName()), packet.getData(), packet.getMap(), true);
        String serverName = null;

        try {
            if (server == null) {
                Logging.IMP.info("No server found for {} with map {}", packet.getName(), packet.getMap());
                Logging.IMP.info("Deploying new server for {} with map {}", packet.getName(), packet.getMap());

                GameBootResponse response = new GameBootResponse(packet.getData().getGameMode(), 1);

                Logging.IMP.info(packet.getData().getGameType().name());

                val duelServer = ServerManager.INSTANCE.getServerByType(GameType.DUELS, 60);
                if (duelServer != null) {
                    Logging.IMP.info("Found best server for duel: {}", duelServer.getName());
                    serverName = duelServer.getChannel().awaitPacket(new ArenaBoot.Request(packet.getData().getGameMode(), packet.getDuelData())).join().getName();
                } else {
                    DeploymentManager.INSTANCE.sendDeploymentRequest(new BukkitDeployRequest(response.getGameMode().bakeData()),
                            ServerDeploy.DeploymentTarget.BUKKIT, false, null, -1,
                            (result) -> {
                                if (result == DeploymentResponse.DENIED_BY_NODE
                                        || result == DeploymentResponse.DEPLOYMENT_TIMEOUT
                                        || result == DeploymentResponse.TRANSACTION_FAILED) {
                                    Logging.IMP.error("Transaction failed! (" + result.name() + ") / (#" + result.transactionId() + ")");
                                } else {
                                    ServerManager.INSTANCE.queueGameInstanceBoot(result.transactionId(), new GameBootResponse[]{response});
                                    Logging.IMP.info("Queued deployment of game instances!");
                                }
                            });
                }
            } else {
                serverName = server.getName();
            }
        } catch (Exception e) {
            Logging.IMP.error("An error occurred while processing the packet: {}", e.getMessage());
        } finally {
            ctx.setResponse(new S37BestDuelServer(serverName));
            ctx.removeLatch();
        }
    }

    @Override
    public void process(C35RemoveDuelRequest packet, ChannelProcessorContext ctx) {
        DuelManager.INSTANCE.removeRequest(packet.getSenderId());
    }
}