package net.skykingdoms.backend.minestom.connection

import net.skykingdoms.backend.base.language.Language
import net.skykingdoms.backend.base.permission.UserRank
import net.skykingdoms.backend.protocol.packet.Packet
import net.skykingdoms.backend.protocol.packet.PacketProcessor
import net.skykingdoms.backend.protocol.packet.play.B101UpdateRank
import net.skykingdoms.backend.protocol.packet.play.B108UpdatePlayerLanguage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class Connection {
    companion object {
        private var connection: LocalClientConnection? = null

        fun connection(connection: LocalClientConnection) {
            this.connection = connection
        }

        fun sendPacket(packet: Packet<*>) {
            connection?.sendPacket(packet)
        }

        fun <A : Packet.Response<*>, B : Packet.Request<*, A>> awaitPacket(packet: B) : CompletableFuture<A>? {
            return connection?.awaitPacket(packet)
        }

        fun <A : Packet.Response<*>, B : Packet.Request<*, A>> awaitPacket(packet: B, timeout: Long) : CompletableFuture<A>? {
            return connection?.awaitPacket(packet, timeout)
        }

        fun updateRank(playerId: Int, action: B101UpdateRank.Action, rank: UserRank) {
            val packet = B101UpdateRank(playerId, action, rank.group.id, rank.id, rank.getExpires(TimeUnit.SECONDS).toInt())
            sendPacket(packet)
        }

        fun updateLanguage(playerId: Int, newLanguage: Language) {
            val packet = B108UpdatePlayerLanguage(playerId, newLanguage.id)
            sendPacket(packet)
        }

        fun registerProcessor(processor: PacketProcessor) {
            if (connection == null) {
                throw IllegalStateException("Connection hasn't been initialized")
            }

            connection?.channel?.registerProcessor(processor)
        }

        private fun connection(): LocalClientConnection {
            if (connection == null) {
                throw IllegalStateException("Connection was not initialized.");
            }

            return connection as LocalClientConnection
        }

        fun isActive(): Boolean {
            return connection != null && connection?.isConnected ?: false;
        }
    }
}