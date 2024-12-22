package net.skykingdoms.backend.minestom.connection

import io.netty.channel.socket.SocketChannel
import net.skykingdoms.api.protocol.logging.Logging
import net.skykingdoms.backend.protocol.channel.AbstractClientChannel
import net.skykingdoms.backend.protocol.channel.AbstractRemoteServerChannel
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext
import net.skykingdoms.backend.protocol.packet.handshake.Handshake
import net.skykingdoms.backend.protocol.packet.play.S04UpdateRemoteData
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl
import org.bukkit.Bukkit
import java.util.EnumSet

class RemoteServerConnection(local: AbstractClientChannel?, channel: SocketChannel?) :
    AbstractRemoteServerChannel(local, channel) {

    override fun onServerDisconnect() {

    }

    override fun active() {
        val flags: EnumSet<Handshake.RequestFlag> = EnumSet.noneOf(Handshake.RequestFlag::class.java)

    }

    override fun process(packet: Handshake.Response?, ctx: ChannelProcessorContext?) {

    }

    override fun process(packet: S04UpdateRemoteData, ctx: ChannelProcessorContext?) {
        val cockData = packet.serverDataUpdate as FluentBukkitDataImpl

        BukkitConnector.INSTANCE.fluentBukkitData().setNodeName(cockData.nodeName)
        BukkitConnector.INSTANCE.fluentBukkitData().setServerName(cockData.serverName)

        Bukkit.getScheduler().runTask(
            BukkitConnector.INSTANCE.plugin()
        ) {
            BukkitConnector.INSTANCE.plugin().getServer().getPluginManager()
                .callEvent(ServerDataUpdateEvent(BukkitConnector.INSTANCE.fluentBukkitData()))
        }

        Logging.IMP.info("Updated server data: {} @ {}", cockData.serverName, cockData.nodeName)
    }
}