package net.skykingdoms.backend.velocity.listener;


import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.protocol.packet.play.*;
import net.skykingdoms.backend.velocity.VelocityConnector;
import net.skykingdoms.backend.velocity.command.VelocityCommandCocker;
import net.skykingdoms.backend.velocity.connection.Connection;
import net.skykingdoms.backend.velocity.event.PlayerConnectedEvent;
import net.skykingdoms.backend.velocity.event.PlayerCoreCommandEvent;
import net.skykingdoms.backend.velocity.gamer.Gamer;
import net.skykingdoms.backend.velocity.punishments.VelocityPunishmentManager;
import net.skykingdoms.backend.velocity.util.ServerUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class PlayerListener {
    Map<InboundConnection, Gamer> loadedData = new ConcurrentHashMap<>();

    // This is used for debug purposes only
    // Shows a time of login process in millis
    Map<String, Long> timings = new ConcurrentHashMap<>();

    @SuppressWarnings("deprecation")
    @Subscribe(order = PostOrder.LAST)
    public void onPreLogin(final @NonNull PreLoginEvent event) {
        /*Logging.IMP.info("Called PreLoginEvent");*/
        if (!Connection.isConnected()) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text(
                            Language.DEFAULT.getMessage(false, "core_not_connected")
                    )
            ));
            return;
        }

        val connection = event.getConnection();
        String playerName = event.getUsername();
        //UUID uuid = event.getUniqueId();

        val playerAddress = connection.getRemoteAddress();

        timings.put(playerName, System.currentTimeMillis());
        Logging.IMP.info("[{}] logged in by address {}", playerName, playerAddress);
    }

    @Subscribe(order = PostOrder.LAST)
    public void onLogin(final @NonNull LoginEvent event, Continuation continuation) {
/*
        Logging.IMP.info("Called LoginEvent");
*/
        if (!event.getResult().isAllowed()) {
            return;
        }

        val connection = event.getPlayer();

        val uuid = connection.getUniqueId();
        val name = connection.getUsername();
        val address = connection.getRemoteAddress().getAddress();

        Logging.IMP.info("Player {} is connecting from {} with uuid {}", name, address, uuid);


        VelocityConnector.INSTANCE.server().getScheduler().buildTask(VelocityConnector.INSTANCE.boot(), () -> {
            val $gamer = Gamer.getOrCreate(uuid, Gamer::loadPlayerId);

            if ($gamer == null || !$gamer.hasPlayerId()) {
                val id = NetworkSqlHandler.INSTANCE.createUserRecord(uuid, name, Language.ENGLISH, connection.getRemoteAddress().getAddress());
/*
                Logging.IMP.info("Registered new player {} -> {}", name, id);
*/
            }

/*
            Logging.IMP.info("User record loaded for {}", name);
*/

            VelocityConnector.INSTANCE.server().getEventManager().fire(new PlayerConnectedEvent(
                    connection,
                    continuation
            )).thenAccept($void -> {
/*
                Logging.IMP.info("PlayerConnectedEvent fired for {}", name);
*/
            });
        }).schedule();
        //continuation.resume();
    }

    @Subscribe
    public void onConnected(PlayerConnectedEvent event) {
/*
        Logging.IMP.info("Called PlayerConnectedEvent");
*/
        Player connection = (Player) event.getConnection();

        val uuid = connection.getUniqueId();

        val gamer = new Gamer(uuid);
        gamer.setProtocol(connection.getProtocolVersion().getProtocol());
        gamer.setDomain(connection.getRemoteAddress().getHostName());
        gamer.setAddress(connection.getRemoteAddress().getAddress());
        gamer.load();

        // Invoking after load
        gamer.updateName(connection.getUsername());

        this.loadedData.put(event.getConnection(), gamer);

        VelocityConnector.INSTANCE.server().getScheduler().buildTask(VelocityConnector.INSTANCE.boot(), () -> {
            // PostLogin didnt pass
            this.loadedData.remove(event.getConnection());
        }).delay(2500L, TimeUnit.MILLISECONDS).schedule();

        // if dude is new to the server we set up his default lang
        if (gamer.getGamerId() < 0) {
            gamer.changeLanguage(Language.DEFAULT, false);
        }

        event.getContinuation().resume();
    }

    @Subscribe(order = PostOrder.LAST)
    public void handlePostLogin(final @NonNull PostLoginEvent e, Continuation continuation) {
/*
        Logging.IMP.info("Called PostLoginEvent");
*/
        val connection = e.getPlayer();
        val gamer = loadedData.remove(connection);

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
                    gamer.getProtocol(), ProxyPlayerLogin.FINISH
            );

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
/*
        Logging.IMP.info("Called PlayerChooseInitialServerEvent");
*/

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
                //newTarget = target;
            }

/*
            Logging.IMP.info("GetBestServer -> {}", newTarget.getServerInfo().getName());
*/

            val request = new ServerConnect.Request(gamer.getGamerId(), newTarget.getServerInfo().getName());

            RegisteredServer targetServer = Connection.awaitPacket(request).thenApply(connectionResponse -> {
/*
                Logging.IMP.info("ServerConnect.Response -> {}", connectionResponse.getNewServer());
*/

                if (connectionResponse.isCancelled()) {
                    player.disconnect(Component.text("§cInternal server error.\n" + "§c[SCRQ-CANCEL]"));
                    continuation.resumeWithException(new RuntimeException("Server connection canceled"));
                } else {
                    return VelocityConnector.INSTANCE.server().getServer(connectionResponse.getNewServer()).get();
                }
                return null;
            }).join();

/*
            Logging.IMP.info("Here.");
*/

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


/*    @Subscribe
    public void onChoose(PlayerChooseInitialServerEvent event) {

    }*/


    @Subscribe(order = PostOrder.LAST)
    public void onServerConnected(ServerConnectedEvent event) {
/*
        Logging.IMP.info("Called ServerConnectedEvent");
*/
        String serverName = event.getServer().getServerInfo().getName();

        Player player = event.getPlayer();
        Gamer gamer = Gamer.getGamer(player);

        if (gamer != null) {
/*
            Logging.IMP.info("Not null.");
*/
            if (gamer.isConnectedToCore()) {
/*
                Logging.IMP.info("Is connected.");
*/
                Connection.sendPacket(new C0PlayerConnected(gamer.getGamerId(), serverName));
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent e) {
/*
        Logging.IMP.info("Called PlayerDisconnectEvent");
*/
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

/*    @Subscribe(order = PostOrder.LAST)
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

*//*        if (!gamer.isLoggedIn()) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }*//*
    }*/

    @Subscribe(order = PostOrder.LAST)
    public void onChat(PlayerChatEvent event) {
        val player = event.getPlayer();
        val gamer = Gamer.getGamer(player);

        if (gamer == null) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

/*        if (!gamer.isLoggedIn()) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }*/

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

/*        if (!event.isCommand() && BungeePunishmentManager.INSTANCE.isMuted(gamer.getGamerId())) {
            gamer.sendMessage(BungeePunishmentManager.INSTANCE.getMuteMessage(gamer));
            event.setCancelled(true);
            return;
        }*/
/*
        if (!event.isCommand()) {
            return;
        }*/

        val message = event.getCommand().trim();
/*
        Logging.IMP.info("Command: {}", message);
*/
        val command = message.split("\\s+")[0].toLowerCase();

        // если команда не зарегистрирована, она не дойдёт до баккита
        if (!VelocityCommandCocker.INSTANCE.getCommands().contains(command)) {
            Logging.IMP.info("Command not found. " + command);
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
