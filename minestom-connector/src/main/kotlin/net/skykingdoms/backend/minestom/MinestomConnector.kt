package net.skykingdoms.backend.minestom

import net.minestom.server.MinecraftServer
import net.skykingdoms.api.backend.util.config.Configuration
import net.skykingdoms.api.backend.util.config.ConfigurationProvider
import net.skykingdoms.api.backend.util.config.YamlConfiguration
import net.skykingdoms.api.protocol.logging.Logging
import net.skykingdoms.api.protocol.server.GameType
import net.skykingdoms.api.protocol.server.ServerScaleType
import net.skykingdoms.api.protocol.type.GameMode
import net.skykingdoms.api.protocol.util.TextFormat
import net.skykingdoms.backend.base.dependency.Dependency
import net.skykingdoms.backend.base.language.Language
import net.skykingdoms.backend.minestom.connection.Connection
import net.skykingdoms.backend.minestom.connection.LocalClientConnection
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl
import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*

class MinestomConnector {
    companion object {
        @Dependency
        val instance: MinestomConnector = MinestomConnector()
    }

    lateinit var instanceData: FluentBukkitDataImpl
    private lateinit var connection: LocalClientConnection
    private var config: Configuration? = null;
    lateinit var secret: String
    lateinit var transactionId: UUID
    var gameMode: GameMode? = null;
    private lateinit var address: SocketAddress
    var testFlowId: Int = -1
    var reconnect = false

    fun init() {
        Language.reloadLanguages()
        loadConfig()

        val env: List<String> = config?.getString("BUKKIT_HOST")?.split(":") ?: System.getenv("BUKKIT_HOST").split(":")
        val port: Int = env[1].toInt()

        gameMode = GameMode.valueOf(System.getProperty("gameMode").uppercase(), null)
        instanceData = FluentBukkitDataImpl(
            gameMode?.name ?: "N/A",
            "N/A",
            ServerScaleType.valueOf(System.getProperty("serverScale").uppercase()),
            GameType.valueOf(System.getProperty("gameType").uppercase(Locale.getDefault()), null),
            System.getProperty("externalAddr"),
            port
        )
        secret = config?.getString("CORD_SECRET") ?: System.getenv("CORD_SECRET")
        transactionId = UUID.fromString(config?.getString("CORD_TID") ?: System.getenv("CORD_TID"))
        testFlowId = System.getProperty("testFlowId", "-1").toInt()
        address = InetSocketAddress(
            System.getProperty("megamindIp"),
            System.getProperty("megamindPort").toInt()
        )

        Connection.connection(connection);
    }

    fun shutdown() {
        for (player in MinecraftServer.getConnectionManager().onlinePlayers) {
            player.kick(TextFormat.formatText(
                "Server {} shut down at {}",
                instanceData.serverName,
                instanceData.nodeName
            ))
        }
    }

    fun loadConfig() {
        val provider: ConfigurationProvider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
        val file: File = File("config.yml")

        if (file.exists()) {
            try {
                config = provider.load(file)
            } catch (e: IOException) {
                Logging.IMP.stacktrace(e, "Can't load the configuration file!")
            }
        }
    }
}