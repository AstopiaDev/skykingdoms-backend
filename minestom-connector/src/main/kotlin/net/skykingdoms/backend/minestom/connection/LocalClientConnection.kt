package net.skykingdoms.backend.minestom.connection

import io.netty.channel.socket.SocketChannel
import net.skykingdoms.backend.protocol.channel.AbstractClientChannel
import net.skykingdoms.backend.protocol.channel.AbstractRemoteServerChannel
import java.net.SocketAddress

class LocalClientConnection(address: SocketAddress?, threads: Int) : AbstractClientChannel(address, threads) {
    override fun newServerChannel(channel: SocketChannel?): AbstractRemoteServerChannel {
        return RemoteServerConnection(this, channel)
    }
}