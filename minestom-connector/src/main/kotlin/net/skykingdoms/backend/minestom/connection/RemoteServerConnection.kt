package net.skykingdoms.backend.minestom.connection

import io.netty.channel.socket.SocketChannel
import net.minestom.server.MinecraftServer
import net.skykingdoms.api.protocol.logging.Logging
import net.skykingdoms.api.protocol.type.RemoteConnectedType
import net.skykingdoms.backend.minestom.HeartBeat
import net.skykingdoms.backend.minestom.MinestomConnector
import net.skykingdoms.backend.minestom.event.ConnectionEstablishedEvent
import net.skykingdoms.backend.minestom.event.InstanceActiveEvent
import net.skykingdoms.backend.minestom.event.InstanceDataUpdateEvent
import net.skykingdoms.backend.minestom.event.InstanceInactiveEvent
import net.skykingdoms.backend.protocol.channel.AbstractClientChannel
import net.skykingdoms.backend.protocol.channel.AbstractRemoteServerChannel
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext
import net.skykingdoms.backend.protocol.packet.PacketProtocol
import net.skykingdoms.backend.protocol.packet.handshake.Handshake
import net.skykingdoms.backend.protocol.packet.play.S04UpdateRemoteData
import net.skykingdoms.backend.protocol.packet.play.S11ServerCommand
import net.skykingdoms.backend.protocol.packet.play.SecurityToken
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl
import java.util.EnumSet

class RemoteServerConnection(local: AbstractClientChannel?, channel: SocketChannel?) :
    AbstractRemoteServerChannel(local, channel) {

    override fun onServerDisconnect() {
        HeartBeat.callEvent(InstanceInactiveEvent())
        MinestomConnector.reconnect = true
        Logging.IMP.info("Disconnected")
    }

    override fun active() {
        HeartBeat.callEvent(InstanceActiveEvent(this))

        val flags: EnumSet<Handshake.RequestFlag> = EnumSet.of<Handshake.RequestFlag>(Handshake.RequestFlag.HAS_FLUENT_DATA)
        if (MinestomConnector.reconnect) {
            flags.add(Handshake.RequestFlag.IS_RECONNECT)
        }
        if (MinestomConnector.testFlowId > -1) {
            flags.add(Handshake.RequestFlag.IS_TESTFLOW)
        }

        sendPacket(Handshake.Request(
            RemoteConnectedType.SERVER,
            MinestomConnector.serverConfig.secret,
            MinestomConnector.serverConfig.transactionId,
            flags,
            MinestomConnector.instanceData,
            MinestomConnector.testFlowId
        ))
    }

    override fun process(packet: Handshake.Response?, ctx: ChannelProcessorContext?) {
        if (packet?.result == Handshake.Result.FAILED) {
            Logging.IMP.error("[HANDSHAKE] Unable to connect to coordinator: {}", packet.message)
            return
        }

        upgradeConnection(PacketProtocol.PLAY)

        Logging.IMP.info("[HANDSHAKE] Upgraded connection to PLAY")

        Connection.awaitPacket(SecurityToken.Request())?.whenComplete { response, throwable ->
            if (throwable != null) {
                Logging.IMP.stacktrace(throwable, "Exception raised on token request! Shutting down the server...");
                MinecraftServer.getServer().stop()
                return@whenComplete
            }

            HeartBeat.callEvent(ConnectionEstablishedEvent(this))
            MinestomConnector.reconnect = false
        }
    }

    override fun process(packet: S11ServerCommand, ctx: ChannelProcessorContext?) {
        MinecraftServer.getCommandManager().executeServerCommand(packet.command)
    }

    override fun process(packet: S04UpdateRemoteData, ctx: ChannelProcessorContext?) {
        val updatedData = packet.serverDataUpdate as FluentBukkitDataImpl
        val currentData = MinestomConnector.instanceData

        currentData.serverName = updatedData.serverName
        currentData.nodeName = updatedData.nodeName

        HeartBeat.callEvent(InstanceDataUpdateEvent(updatedData))
        Logging.IMP.info("Updated instance data: {} @ {}", currentData.serverName, currentData.nodeName)
    }
}