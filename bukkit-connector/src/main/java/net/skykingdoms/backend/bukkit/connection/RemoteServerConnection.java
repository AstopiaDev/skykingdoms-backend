package net.skykingdoms.backend.bukkit.connection;

import io.netty.channel.socket.SocketChannel;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.bukkit.BukkitConnector;
import net.skykingdoms.backend.bukkit.event.ConnectionEstablishedEvent;
import net.skykingdoms.backend.bukkit.event.ServerActiveEvent;
import net.skykingdoms.backend.bukkit.event.ServerDataUpdateEvent;
import net.skykingdoms.backend.bukkit.event.ServerInactiveEvent;
import net.skykingdoms.backend.bukkit.security.BukkitSecurityManager;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteServerChannel;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.PacketProtocol;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import net.skykingdoms.backend.protocol.packet.play.S04UpdateRemoteData;
import net.skykingdoms.backend.protocol.packet.play.S11ServerCommand;
import net.skykingdoms.backend.protocol.packet.play.S15ReloadLanguages;
import net.skykingdoms.backend.protocol.packet.play.SecurityToken;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl;
import org.bukkit.Bukkit;

import java.util.EnumSet;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RemoteServerConnection extends AbstractRemoteServerChannel { public RemoteServerConnection(final @NonNull BukkitLocalClientConnection local, final @NonNull SocketChannel channel) {
        super(local, channel);
    }

    @Override
    public void onServerDisconnect() {
        BukkitConnector.INSTANCE.plugin().getServer().getPluginManager().callEvent(new ServerInactiveEvent());
        BukkitConnector.INSTANCE.reconnecting(true);
    }

    @Override
    public void active() {
        try {
            BukkitConnector.INSTANCE.plugin().getServer().getPluginManager().callEvent(new ServerActiveEvent(this));
        } catch (Throwable t) {
            Logging.IMP.stacktrace(t, "Can't call active event");
        }

        EnumSet<Handshake.RequestFlag> flags = EnumSet.noneOf(Handshake.RequestFlag.class);
        if (BukkitConnector.INSTANCE.reconnecting()) {
            flags.add(Handshake.RequestFlag.IS_RECONNECT);
        }
        if (BukkitConnector.INSTANCE.testFlowId() > -1) {
            flags.add(Handshake.RequestFlag.IS_TESTFLOW);
        }
        flags.add(Handshake.RequestFlag.HAS_FLUENT_DATA);

        sendPacket(new Handshake.Request(
                RemoteConnectedType.SERVER,
                BukkitConnector.INSTANCE.secret(),
                BukkitConnector.INSTANCE.transactionId(),
                flags,
                BukkitConnector.INSTANCE.fluentBukkitData(),
                BukkitConnector.INSTANCE.testFlowId()));
    }

    @Override
    public void process(final @NonNull Handshake.Response packet,
                        final @NonNull ChannelProcessorContext ctx) {
        if (packet.getResult() == Handshake.Result.FAILED) {
            Logging.IMP.error("[HANDSHAKE] Unable to connect to coordinator: {}", packet.getMessage());
            return;
        }

        upgradeConnection(PacketProtocol.PLAY);

        Connection.awaitPacket(new SecurityToken.Request()).whenComplete((response, throwable) -> {
            if (throwable != null) {
                Logging.IMP.stacktrace(throwable, "Exception raised on token request! Shutting down the proxy...");
                Bukkit.shutdown();
                return;
            }

            BukkitSecurityManager.INSTANCE.updateToken(response.getToken());
        });

        BukkitConnector.INSTANCE.plugin().getServer().getPluginManager().callEvent(new ConnectionEstablishedEvent(this));
        BukkitConnector.INSTANCE.reconnecting(false);
    }

    @Override
    public void process(final @NonNull S11ServerCommand packet, final @NonNull ChannelProcessorContext ctx) {
        val command = packet.getCommand();
        BukkitConnector.INSTANCE.plugin().getServer().dispatchCommand(BukkitConnector.INSTANCE.plugin().getServer().getConsoleSender(), command);
    }

    @Override
    public void process(final S04UpdateRemoteData packet, ChannelProcessorContext ctx) {
        val cockData = (FluentBukkitDataImpl) packet.getServerDataUpdate();

        BukkitConnector.INSTANCE.fluentBukkitData().setNodeName(cockData.getNodeName());
        BukkitConnector.INSTANCE.fluentBukkitData().setServerName(cockData.getServerName());

        Bukkit.getScheduler().runTask(BukkitConnector.INSTANCE.plugin(), () -> {
            BukkitConnector.INSTANCE.plugin().getServer().getPluginManager().callEvent(new ServerDataUpdateEvent(BukkitConnector.INSTANCE.fluentBukkitData()));
        });

        Logging.IMP.info("Updated server data: {} @ {}", cockData.getServerName(), cockData.getNodeName());
    }

    @Override
    public void process(S15ReloadLanguages packet, ChannelProcessorContext ctx) {
        Language.reloadLanguages();
    }
}
