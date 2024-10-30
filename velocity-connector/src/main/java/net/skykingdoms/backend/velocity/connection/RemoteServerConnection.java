package net.skykingdoms.backend.velocity.connection;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import io.netty.channel.socket.SocketChannel;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.data.state.NetworkState;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteServerChannel;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.PacketProtocol;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import net.skykingdoms.backend.protocol.packet.play.*;
import net.skykingdoms.backend.protocol.type.reconnect.ReconnectingGamer;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentProxyDataImpl;
import net.skykingdoms.backend.velocity.VelocityConnector;
import net.skykingdoms.backend.velocity.command.FakeCommandProxy;
import net.skykingdoms.backend.velocity.command.VelocityCommandCocker;
import net.skykingdoms.backend.velocity.event.ServerActiveEvent;
import net.skykingdoms.backend.velocity.event.ServerInactiveEvent;
import net.skykingdoms.backend.velocity.gamer.Gamer;
import net.skykingdoms.backend.velocity.punishments.VelocityPunishmentManager;
import net.skykingdoms.backend.velocity.punishments.MinimalMuteData;
import net.skykingdoms.backend.velocity.security.BungeeSecurityManager;

import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class RemoteServerConnection extends AbstractRemoteServerChannel {
    public RemoteServerConnection(final @NonNull SocketChannel channel) {
        super(VelocityConnector.INSTANCE.connection(), channel);
    }

    @Override
    public void process(S28UpdateNetworkState packet, ChannelProcessorContext ctx) {
        val state = NetworkState.getById(packet.getNetworkState());

        // updating active network state
        VelocityConnector.INSTANCE.networkState(state);

        // calling "onEnable" consumer
        Gamer.getGamers().forEach(gamer -> state.getOnJoin().accept(gamer));
    }

    @Override
    public void process(final @NonNull B107ProxyKick packet,
                        final @NonNull ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        // null maybe
        if (gamer != null && gamer.isOnline()) {
            Logging.IMP.info("Kicked {} for {}", gamer.getName(), packet.getReason());
            gamer.getPlayer().disconnect(Component.text(packet.getReason()));
        }
    }

    @Override
    public void process(Handshake.Response packet, ChannelProcessorContext ctx) {
        if (packet.getResult() != Handshake.Result.SUCCESS) {
            Logging.IMP.warn("[HANDSHAKE] Unable to connect to coordinator: {}", packet.getMessage());
            return;
        }

        Logging.IMP.info("[HANDSHAKE] Connected to coordinator successfully!");

        upgradeConnection(PacketProtocol.PLAY);

        sendPacket(new C17LookupGlobalOnline());

        Connection.awaitPacket(new SecurityToken.Request()).whenComplete((response, throwable) -> {
            if (throwable != null) {
                Logging.IMP.stacktrace(throwable, "Exception raised on token request! Shutting down the proxy...");
                VelocityConnector.INSTANCE.server().shutdown();
                return;
            }

            BungeeSecurityManager.INSTANCE.updateToken(response.getToken());
            BungeeSecurityManager.INSTANCE.updatePlayersConnections();
        });

        reconnectPlayers();
        VelocityConnector.INSTANCE.reconnecting(false);
    }

    @Override
    public void process(S12FailedReconnectPlayers packet, ChannelProcessorContext ctx) {
        for (val reconnectGamer : packet.getGamers()) {
            val player = VelocityConnector.INSTANCE.server().getPlayer(reconnectGamer.getName()).orElse(null);

            if (player == null) {
                continue;
            }

            player.disconnect(Component.text(reconnectGamer.getReason()));
        }
    }

    private void reconnectPlayers() {
        val gamers = Gamer.getGamers();

        gamers.stream().filter(g -> !g.isOnline())
                .forEach(Gamer::removeGamer);

        if (gamers.isEmpty()) {
            return;
        }

        Logging.IMP.info("[RECONNECT] Trying to reconnect {} players", gamers.size());

        val reconnectingGamers = new HashSet<ReconnectingGamer>();

        for (val gamer : gamers) {
            val player = gamer.getPlayer();

            val name = gamer.getName();
            val uuid = gamer.getPlayerUniqueId();
            val address = player.getRemoteAddress().getAddress();

            val serverName = player.getCurrentServer().orElse(null).getServerInfo().getName();

            reconnectingGamers.add(new ReconnectingGamer(name, uuid, address, gamer.getDomain(), gamer.getProtocol(), gamer.isLoggedIn(), serverName));
        }

        sendPacket(new C10ReconnectPlayers(reconnectingGamers));
    }

    @Override
    public void active() {
        channel.eventLoop().execute(() -> {
            VelocityConnector.INSTANCE.server().getEventManager().fire(new ServerActiveEvent(this));

            upgradeConnection(PacketProtocol.HANDSHAKE);

            EnumSet<Handshake.RequestFlag> flags = EnumSet.noneOf(Handshake.RequestFlag.class);
            if (VelocityConnector.INSTANCE.reconnecting()) {
                flags.add(Handshake.RequestFlag.IS_RECONNECT);
            }
            flags.add(Handshake.RequestFlag.HAS_FLUENT_DATA);

            sendPacket(new Handshake.Request(
                    RemoteConnectedType.PROXY,
                    System.getenv("CORD_SECRET"),
                    UUID.fromString(System.getenv("CORD_TID")),
                    flags,
                    VelocityConnector.INSTANCE.proxyData(), 0));
        });
    }


    @Override
    public void process(S07UnregisterServer packet, ChannelProcessorContext ctx) {
        String name = packet.getServerName();
        RegisteredServer server = VelocityConnector.INSTANCE.server().getServer(name).orElse(null);

        if (server == null) {
            return;
        }

        VelocityConnector.INSTANCE.server().unregisterServer(server.getServerInfo());
        Logging.IMP.info("Server {} has been unregistered", name);
    }

    @Override
    public void process(S06RegisterServers packet, ChannelProcessorContext ctx) {
        for (val info : packet.getServers()) {
            val name = info.getServerName();
            val ip = info.getServerAddress();
            val scale = info.getScaleType();
            val type = info.getScaleType();

            val port = info.getServerPort();

            Logging.IMP.info("Server {} has been registered. S: {} @ T: {} @ A: {}",
                    name, scale, type, ip + ":" + port);

            InetSocketAddress address = new InetSocketAddress(ip, port);

            VelocityConnector.INSTANCE.server().registerServer(
                    new ServerInfo(name, address)
            );
        }
    }

    @Override
    public void onServerDisconnect() {
        VelocityConnector.INSTANCE.reconnecting(true);
        VelocityConnector.INSTANCE.server().getEventManager().fire(new ServerInactiveEvent(this));
    }


    @Override
    public void process(S18SendPlayer packet, ChannelProcessorContext ctx) {
        int playerId = packet.getGamerId();

        val gamer = Gamer.getGamer(packet.getGamerId());

        val name = gamer == null
                ? NetworkSqlHandler.INSTANCE.getPlayerName(playerId)
                : gamer.getName();

        val player = VelocityConnector.INSTANCE.server().getPlayer(name).orElse(null);

        if (player == null) {
            return;
        }

        val server = VelocityConnector.INSTANCE.server().getServer(packet.getServer()).orElse(null);

        if (server == null) {
            return;
        }

        player.createConnectionRequest(server).connect().join();
    }

    @Override
    public void process(S04UpdateRemoteData packet, ChannelProcessorContext ctx) {
        val updated = (FluentProxyDataImpl) packet.getServerDataUpdate();

        VelocityConnector.INSTANCE.proxyData().setProxyName(updated.getProxyName());
        VelocityConnector.INSTANCE.proxyData().setNodeName(updated.getNodeName());

        Logging.IMP.info("Updated proxy data: {} @ {}",
                VelocityConnector.INSTANCE.proxyData().getProxyName(),
                VelocityConnector.INSTANCE.proxyData().getNodeName());
    }

    @Override
    public void process(S15ReloadLanguages packet, ChannelProcessorContext ctx) {
        Language.reloadLanguages();
    }

    @Override
    public void process(final @NonNull B108UpdatePlayerLanguage packet,
                        final @NonNull ChannelProcessorContext ctx) {
        val player = Gamer.getGamer(packet.getPlayerId());

        if (player == null) {
            return;
        }

        val language = Language.getById(packet.getLanguageId());
        player.changeLanguage(language, false);
    }

    @Override
    public void process(S24PlayerMuteAction packet, ChannelProcessorContext ctx) {
        if (packet.getAction() == S24PlayerMuteAction.Action.REMOVE_MUTE) {
            VelocityPunishmentManager.INSTANCE.removeMute(packet.getMuteId());
        }

        if (packet.getAction() == S24PlayerMuteAction.Action.ADD_MUTE) {
            VelocityPunishmentManager.INSTANCE.addMute(new MinimalMuteData(
                    packet.getMuteId(),
                    packet.getViolatorId(),
                    packet.getReason(),
                    packet.getExpiration()
            ));
        }
    }

    @Override
    public void process(S13SendTitle packet, ChannelProcessorContext ctx) {
        Gamer gamer = Gamer.getGamer(packet.getReceiver());

        if (gamer != null) {
            gamer.sendTitle(packet.getTitle(), packet.getSubtitle(), packet.getFadeIn(), packet.getStay(), packet.getFadeOut());
        }
    }

    @Override
    public void process(final B104BroadcastChat packet, ChannelProcessorContext ctx) {
/*        BaseComponent[] comp = processPacketMessage(packet.getMessage(), true);

        for (val player : VelocityConnector.INSTANCE.getProxy().getPlayers()) {
            player.sendMessage(comp);
        }*/
    }

/*    private Component[] processPacketMessage(String msg, boolean tryJson) {
        val escapedMessage = msg.trim();

        Component[] comp;

        if (tryJson && ((escapedMessage.startsWith("{") && escapedMessage.endsWith("}"))
                || (escapedMessage.startsWith("[") && escapedMessage.endsWith("]")))) {
            comp = ComponentSerializer.parse(escapedMessage);
        } else {
            comp = TextComponent.fromLegacyText(msg);
        }

        return comp;
    }*/

    //TODO: FIX THIS BASICALLY WHEN CMD SENT ITS USING THIS PACKET I NEED TO FIX DOLBABGE
    @Override
    public void process(final @NonNull B106Chat packet,
                        final @NonNull ChannelProcessorContext ctx) {
        val player = VelocityConnector.INSTANCE.server().getPlayer(NetworkSqlHandler.INSTANCE.getPlayerName(packet.getReceiver()));
        if (player.isPresent()) {
            if (packet.isJson()) {
                player.get().sendMessage(JSONComponentSerializer.json().deserialize(packet.getMessage()));
            } else {
                player.get().sendMessage(Component.text(packet.getMessage()));/*sendRichMessage(processPacketMessage(packet.getMessage(), packet.isJson()));*/
            }
        }
    }

    @Override
    public void process(S10RegisterCommands packet, ChannelProcessorContext ctx) {
        for (val cont : packet.getCommands()) {
            switch (cont.getSource()) {
                case CORE: {
                    VelocityCommandCocker.INSTANCE.setCommands(cont.getCommands());
                    for (String command : cont.getCommands()) {
                        com.velocitypowered.api.command.CommandManager manager = VelocityConnector.INSTANCE.server().getCommandManager();
                        CommandMeta meta = manager
                                .metaBuilder(command)
                                .build();

                        VelocityConnector.INSTANCE.server().getCommandManager().register(meta, new FakeCommandProxy());
                    }
                    break;
                }
                case SERVER: {
                    VelocityCommandCocker.INSTANCE.getServerCommands().put(VelocityConnector.INSTANCE.server().getServer(cont.getServerName()).orElse(null).getServerInfo(), cont.getCommands());
                    break;
                }
            }
        }
    }

    @Override
    public void process(S14SetPlayerTablist packet, ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        val player = gamer.getPlayer();

        player.sendPlayerListHeaderAndFooter(
                Component.text(packet.getHeader()),
                Component.text(packet.getFooter())
        );
    }

    @Override
    public void process(final @NonNull B102SetProperty packet,
                        final @NonNull ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        gamer.updateProperty(packet.getKey(), packet.getProperty());
    }

    @Override
    public void process(final @NonNull B103RemoveProperty packet,
                        final @NonNull ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        gamer.dropProperty(packet.getProperty());
    }

    @Override
    public void process(final @NonNull B101UpdateRank packet,
                        final @NonNull ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        val groupId = packet.getGroupId();
        val rankId = packet.getRankId();
        val expires = packet.getExpires();

        val rank = new UserRank(rankId, Group.getById(groupId), expires);

        switch (packet.getAction()) {
            case ADD_RANK: {
                gamer.addRank(rank);
                break;
            }
            case REMOVE_RANK: {
                gamer.removeRank(rank);
                break;
            }
            case SET_RANK: {
                gamer.setRank(rank);
                break;
            }
        }
    }

    @Override
    public void process(final @NonNull S39UpdateAuth packet,
                        final @NonNull ChannelProcessorContext ctx) {
        val gamer = Gamer.getGamer(packet.getPlayerId());

        if (gamer == null) {
            return;
        }

        switch (packet.getAction()) {
            case LOGIN: {
                gamer.setLoggedIn(true);
                break;
            }
            case LOGOUT: {
                gamer.setLoggedIn(false);
                break;
            }
        }
    }
}