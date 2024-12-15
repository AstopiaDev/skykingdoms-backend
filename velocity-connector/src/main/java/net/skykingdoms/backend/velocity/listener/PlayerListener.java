package net.skykingdoms.backend.velocity.listener;


import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.api.util.UuidUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.base.data.auth.AuthSqlHandler;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.util.optionals.Optional;
import net.skykingdoms.backend.protocol.packet.play.*;
import net.skykingdoms.backend.velocity.VelocityConnector;
import net.skykingdoms.backend.velocity.command.VelocityCommandCocker;
import net.skykingdoms.backend.velocity.connection.Connection;
import net.skykingdoms.backend.velocity.event.PlayerConnectedEvent;
import net.skykingdoms.backend.velocity.event.PlayerCoreCommandEvent;
import net.skykingdoms.backend.velocity.gamer.Gamer;
import net.skykingdoms.backend.velocity.punishments.VelocityPunishmentManager;
import net.skykingdoms.backend.velocity.util.Profile;
import net.skykingdoms.backend.velocity.util.ServerUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class PlayerListener {
    Map<String, Gamer> loadedData = new ConcurrentHashMap<>();

    // This is used for debug purposes only
    // Shows a time of login process in millis
    Map<String, Long> timings = new ConcurrentHashMap<>();
    Map<String, UUID> premiumUUIDCache = new ConcurrentHashMap<>();

    @SuppressWarnings("deprecation")
    @Subscribe(order = PostOrder.LAST)
    public EventTask onPreLogin(final @NonNull PreLoginEvent event) {
        if (!Connection.isConnected()) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text(
                            Language.DEFAULT.getMessage(false, "core_not_connected")
                    )
            ));
            return null;
        }

        val connection = event.getConnection();
        val playerAddress = connection.getRemoteAddress();
        val playerName = event.getUsername();

        return EventTask.async(() -> {
            handleAuthentication(event);
            timings.put(playerName, System.currentTimeMillis());
            Logging.IMP.info("[{}] logged in by address {}", playerName, playerAddress);
        });
    }

    private void handleAuthentication(PreLoginEvent event) {
        val id = NetworkSqlHandler.INSTANCE.getPlayerId(event.getUsername());
        val premium = id != -1 ? AuthSqlHandler.INSTANCE.isPremium(id).join() : null;

        if (premium != null) {
            Gamer gamer = new Gamer(id);
            gamer.setPremium(premium);
            gamer.setLoggedIn(premium);

            loadedData.put(event.getUsername(), gamer);

            if (premium) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            }
        }
    }

    @Subscribe
    public void onGameProfileRequest(GameProfileRequestEvent event) {
        if (event.isOnlineMode()) {
            Gamer session;

            if (!loadedData.containsKey(event.getUsername())) {
                try {
                    Optional<Profile> mcProfile = VelocityConnector.INSTANCE.authenticator().findProfile(event.getUsername());
                    if (mcProfile.isPresent())  {
                        Profile profile = mcProfile.get();
                        session = new Gamer(UuidUtils.fromUndashed(profile.id));
                        session.setPremium(true);
                        session.setLoggedIn(true);

                        loadedData.put(event.getUsername(), session);
                    } else {
                        if (event.getConnection() instanceof Player player) {
                            player.disconnect(Component.text("§cAn error occured while trying to conect you to the server\n §c[VelocityAuth/PL]\n§r\n§cContact support at discord.skykingdoms.net"));
                        }
                    }
                } catch (Exception e) {

                }
            }

            List<GameProfile.Property> newProp = removeSkin(event.getGameProfile().getProperties());
            event.setGameProfile(event.getGameProfile().withProperties(newProp));
        } else {
            Logging.IMP.info("Is offline mode");
        }
    }

    private List<GameProfile.Property> removeSkin(Collection<GameProfile.Property> oldProperties) {
        List<GameProfile.Property> newProperties = new ArrayList<>(oldProperties.size());
        for (GameProfile.Property property : oldProperties) {
            if (!"textures".equals(property.getName())) {
                newProperties.add(property);
            }
        }

        return newProperties;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onLogin(final @NonNull LoginEvent event, Continuation continuation) {
        if (!event.getResult().isAllowed()) {
            return;
        }

        val connection = event.getPlayer();

        val uuid = connection.getUniqueId();
        val name = connection.getUsername();
        val address = connection.getRemoteAddress().getAddress();

        Logging.IMP.info("Player {} is connecting from {} with uuid {}", name, address, uuid);


        VelocityConnector.INSTANCE.server().getScheduler().buildTask(VelocityConnector.INSTANCE.boot(), () -> {
            var $gamer = loadedData.get(name);

            if ($gamer != null) {
                if (!$gamer.hasPlayerId()) {
                    $gamer.loadPlayerId();
                } else {
                    $gamer.setPlayerUniqueId(uuid);
                }
            } else {
                $gamer = Gamer.getOrCreate(uuid, Gamer::loadPlayerId);
            }

            if ($gamer == null || !$gamer.hasPlayerId()) {
                int gamerId = NetworkSqlHandler.INSTANCE.createUserRecord(uuid, name, Language.ENGLISH, connection.getRemoteAddress().getAddress());
                if ($gamer != null && $gamer.isPremium() && $gamer.isLoggedIn()) {
                    AuthSqlHandler.INSTANCE.insertAuthRecord(gamerId, true, null, null);
                }
            }

            VelocityConnector.INSTANCE.server().getEventManager().fire(new PlayerConnectedEvent(
                    connection,
                    continuation
            )).join();
        }).schedule();
    }

    @Subscribe
    public void onConnected(PlayerConnectedEvent event) {
        Player connection = (Player) event.getConnection();

        val uuid = connection.getUniqueId();

        var gamer = loadedData.get(connection.getUsername());
        if (gamer == null) {
            gamer = new Gamer(uuid);
        }

        gamer.setProtocol(connection.getProtocolVersion().getProtocol());
        gamer.setDomain(connection.getRemoteAddress().getHostName());
        gamer.setAddress(connection.getRemoteAddress().getAddress());
        gamer.load();

        gamer.updateName(connection.getUsername());
        this.loadedData.put(connection.getUsername(), gamer);

        VelocityConnector.INSTANCE.server().getScheduler().buildTask(VelocityConnector.INSTANCE.boot(), () -> {
            this.loadedData.remove(connection.getUsername());
        }).delay(2500L, TimeUnit.MILLISECONDS).schedule();

        if (gamer.getGamerId() < 0) {
            gamer.changeLanguage(Language.DEFAULT, false);
        }

        event.getContinuation().resume();
    }

    @Subscribe(order = PostOrder.LAST)
    public void handlePostLogin(final @NonNull PostLoginEvent e, Continuation continuation) {
        val connection = e.getPlayer();
        val gamer = loadedData.remove(connection.getUsername());

        if (gamer == null) {
            connection.disconnect(Component.text("§cData was not loaded [VelocityAuth/PL]"));
            return;
        }

        Gamer.addGamer(gamer);

        if (!gamer.isConnectedToCore()) {
            val uuid = connection.getUniqueId();
            val name = connection.getUsername();
            val address = connection.getRemoteAddress().getAddress();

            val loginFinishPacket = new ProxyPlayerLogin.Request(
                    uuid, name, address, gamer.getDomain(),
                    gamer.getProtocol(), gamer.isPremium(),
                    ProxyPlayerLogin.FINISH
            );

            Logging.IMP.info("Premium: {}, Logged in: {}", gamer.isPremium(), gamer.isLoggedIn());

            // we can invalidate the gamer mute, because it may already expire
            // the gamer is not connected to VelocityConnector.INSTANCE.boot() yet
            VelocityPunishmentManager.INSTANCE.invalidateMetas(gamer);

            Connection.awaitPacket(loginFinishPacket).whenComplete((response, cause) -> {
                if (cause != null) {
                    connection.disconnect(Component.text(
                            "§cData was not loaded [B-SCE]"
                    ));
                    return;
                }

                if (response.isCancelled()) {
                    connection.disconnect(Component.text(response.getMessage()));
                    return;
                }

                if (response.isSessionLogin()) {
                    gamer.setLoggedIn(true);
                }

                long time = -1;

                if (timings.containsKey(name)) {
                    time = timings.remove(name);
                }

                Logging.IMP.info("[{}] log in time -> {} ms", gamer.getName(), System.currentTimeMillis() - time);

                gamer.setConnectedToCore(true);
                continuation.resume();
            }).exceptionally(throwable -> {
                connection.disconnect(Component.text("§cInternal server error.\n" + "§c[PL-EX]"));
                Logging.IMP.stacktrace(throwable, "Error while handling PostLoginEvent");
                return null;
            });
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onChoose(PlayerChooseInitialServerEvent event, Continuation continuation) {
        val player = event.getPlayer();
        val gamer = Gamer.getGamer(player.getUniqueId());

        gamer.setPlayer(player);

        val target = event.getInitialServer().orElse(null);

        // this always happens if player just joins the server
        // name of the default server is "undefined"
        // so we ask a coordinator to get us a new server uwu owo >.<

        if (target == null || target.getServerInfo().getName().equals("undefined")) {
            RegisteredServer newTarget = ServerUtil.getServerToConnect(gamer, new ScalableDataImpl(ServerScaleType.LOBBY, GameType.MAIN, null)).join();
            if (newTarget == null) {
                player.disconnect(Component.text("§cInternal server error.\n" + "§c[SCRQ-CANCEL]"));
                continuation.resume();
                return;
            }

            val request = new ServerConnect.Request(gamer.getGamerId(), newTarget.getServerInfo().getName());

            RegisteredServer targetServer = Connection.awaitPacket(request).thenApply(connectionResponse -> {
                if (connectionResponse.isCancelled()) {
                    player.disconnect(Component.text("§cInternal server error.\n" + "§c[SCRQ-CANCEL]"));
                    continuation.resumeWithException(new RuntimeException("Server connection canceled"));
                } else {
                    return VelocityConnector.INSTANCE.server().getServer(connectionResponse.getNewServer()).get();
                }
                return null;
            }).join();

            if (targetServer == null || targetServer.getServerInfo().getName().equals("undefined")) {
                player.disconnect(Component.text("§cInternal server error.\n" + "§c[NSF]"));
                continuation.resumeWithException(new RuntimeException("No valid target server"));
            } else {
                event.setInitialServer(targetServer);
                continuation.resume();
            }
        } else {
            player.disconnect(Component.text("§cInternal server error.\n" + "§c[SCRQ-CANCEL]"));
        }
    }


    @Subscribe(order = PostOrder.LAST)
    public void onServerConnected(ServerConnectedEvent event) {
        String serverName = event.getServer().getServerInfo().getName();

        Player player = event.getPlayer();
        Gamer gamer = Gamer.getGamer(player);

        if (gamer != null) {
            if (gamer.isConnectedToCore()) {
                Connection.sendPacket(new C0PlayerConnected(gamer.getGamerId(), serverName));
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent e) {
        String name = e.getPlayer().getUsername();

        Gamer gamer = Gamer.getGamer(name);

        if (gamer != null) {

            VelocityPunishmentManager.INSTANCE.invalidateMetas(gamer);


            Gamer.removeGamer(gamer);

            if (Connection.isConnected()) {
                Connection.sendPacket(new C12ProxyPlayerDisconnect(gamer.getGamerId()));
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onChat(PlayerChatEvent event) {
        val player = event.getPlayer();
        val gamer = Gamer.getGamer(player);

        if (gamer == null) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        if (!gamer.isLoggedIn()) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        if (event.getMessage().startsWith("/")) {
            return;
        }

        if (VelocityPunishmentManager.INSTANCE.isMuted(gamer.getGamerId())) {
            gamer.sendMessage(VelocityPunishmentManager.INSTANCE.getMuteMessage(gamer));
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onCommand(CommandExecuteEvent event) {
        CommandSource source = event.getCommandSource();

        if (!(source instanceof Player player)) {
            return;
        }

        val gamer = Gamer.getGamer(player);

        if (gamer == null) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        val message = event.getCommand().trim();
        val command = message.split("\\s+")[0].toLowerCase();

        if (!VelocityCommandCocker.INSTANCE.getCommands().contains(command)) {
            if (!gamer.isLoggedIn()) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());
            }
            return;
        }

        event.setResult(CommandExecuteEvent.CommandResult.denied());

        val coreCommandEvent = new PlayerCoreCommandEvent(player, command);
        VelocityConnector.INSTANCE.server().getEventManager().fire(coreCommandEvent);

        if (coreCommandEvent.isCancelled()) {
            return;
        }

        Connection.sendPacket(new C09PlayerCommand(gamer.getGamerId(), "/" + message));
    }
}
