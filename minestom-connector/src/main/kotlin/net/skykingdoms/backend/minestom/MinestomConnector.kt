package net.skykingdoms.backend.minestom

import net.lingala.zip4j.ZipFile
import net.minestom.server.MinecraftServer
import net.skykingdoms.api.backend.util.config.*
import net.skykingdoms.api.protocol.logging.Logging
import net.skykingdoms.api.protocol.server.GameType
import net.skykingdoms.api.protocol.server.ServerScaleType
import net.skykingdoms.api.protocol.type.GameMode
import net.skykingdoms.api.protocol.util.TextFormat
import net.skykingdoms.backend.base.language.Language
import net.skykingdoms.backend.minestom.connection.Connection
import net.skykingdoms.backend.minestom.connection.LocalClientConnection
import net.skykingdoms.backend.protocol.exception.ConnectException
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object MinestomConnector {
    private const val CONFIG_FILE = "config.yml"
    private const val ZIP_EXTENSION = ".zip"

    data class ServerConfig(
        val apiBaseUrl: String,
        val authValue: String,
        val coordinatorIp: String,
        val coordinatorPort: Int,
        val secret: String,
        val transactionId: UUID,
        val bukkitHost: String,
        val externalAddr: String
    )

    lateinit var instanceData: FluentBukkitDataImpl
    private lateinit var connection: LocalClientConnection
    var gameMode: GameMode? = null
    var testFlowId: Int = -1
    var reconnect = false

    val serverConfig: ServerConfig by lazy { initializeServerConfig() }

    private val config: Configuration? by lazy { loadConfig() }
    private val httpClient = HttpClient.newHttpClient()

    fun init() {
        Language.reloadLanguages()
        instanceData = initializeInstanceData()
        logInitialization()

        downloadAssets()
        processTestFlowIfNeeded()

        connection = LocalClientConnection(
            InetSocketAddress(serverConfig.coordinatorIp, serverConfig.coordinatorPort), 2
        );

        HeartBeat.runSync {
            try {
                connection.connectSynchronized()
                Logging.IMP.info("[connection] Synchronized")
                Logging.IMP.info("[boot] Executing core bootstrap [...]")
                Connection.connection(connection)
            } catch (e: ConnectException) {
                e.printStackTrace()
            }
        }
    }

    fun isGameServer() =
        (instanceData.serverScaleType == ServerScaleType.MEGA ||
                instanceData.serverScaleType == ServerScaleType.MINI) &&
                gameMode != null

    fun isLobby() = instanceData.serverScaleType == ServerScaleType.LOBBY

    fun getPort() = serverConfig.bukkitHost.split(":")[1].toInt()

    fun getLocalHost() = serverConfig.bukkitHost.split(":")[0]

    fun isTestFlow() = System.getenv().containsKey("TEST_FLOW")

    fun getTestFlowName(): String? = if (isTestFlow()) System.getenv("TEST_FLOW") else null

    fun shutdown() {
        MinecraftServer.getConnectionManager().onlinePlayers.forEach { player ->
            player.kick(
                TextFormat.formatText(
                    "Server {} shut down at {}",
                    instanceData.serverName,
                    instanceData.nodeName
                )
            )
        }
    }

    private fun initializeServerConfig(): ServerConfig {
        return ServerConfig(
            apiBaseUrl = config?.getString("api_url") ?: throw IllegalStateException("API Base URL not found in config"),
            authValue = config?.getString("auth_value") ?: throw IllegalStateException("Auth value not found in config"),
            coordinatorIp = config?.getString("coordinator_ip") ?: throw IllegalStateException("Coordinator ip not found in config"),
            coordinatorPort = config?.getInt("coordinator_port") ?: throw IllegalStateException("Coordinator port not found in config"),
            secret = config?.getString("CORD_SECRET") ?: System.getenv("CORD_SECRET") ?: "unknown",
            transactionId = UUID.fromString(config?.getString("CORD_TID") ?: System.getenv("CORD_TID")),
            bukkitHost = config?.getString("BUKKIT_HOST") ?: System.getenv("BUKKIT_HOST"),
            externalAddr = externalHost()
        )
    }

    private fun initializeInstanceData(): FluentBukkitDataImpl {
        gameMode = GameMode.valueOf(
            config?.getString("GAME_MODE") ?: System.getenv("GAME_MODE"),
            null
        )
        return FluentBukkitDataImpl(
            gameMode?.name ?: "N/A",
            "N/A",
            ServerScaleType.valueOf(config?.getString("SERVER_SCALE") ?: System.getenv("SERVER_SCALE")),
            GameType.valueOf(
                config?.getString("GAME_TYPE") ?: System.getenv("GAME_TYPE"),
                null
            ),
            serverConfig.externalAddr,
            serverConfig.bukkitHost.split(":")[1].toInt()
        )
    }

    private fun loadConfig(): Configuration? {
        return try {
            val file = File(CONFIG_FILE)
            if (file.exists()) {
                ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(file)
            } else null
        } catch (e: Exception) {
            Logging.IMP.stacktrace(e, "Can't load the configuration file!")
            null
        }
    }

    private fun logInitialization() {
        Logging.IMP.info("[boot] Dumped ENV: {}", System.getenv().entries.joinToString(", ") { "${it.key}=${it.value}" })
        Logging.IMP.info("[boot] Agent args: {} @ {} @ {}",
            instanceData.serverScaleType, instanceData.gameType, gameMode)
    }

    private fun downloadAssets() {
        Logging.IMP.info("[boot] Downloading global assets [...]")
        downloadAndExtractAssets(gameType = instanceData.gameType, outputPrefix = "stash")
        Logging.IMP.info("[boot] Assets downloaded [ok]")

        if (isLobby()) {
            Logging.IMP.info("[boot] Detected lobby configuration, downloading assets [...]")
            downloadAndExtractAssets(gameType = instanceData.gameType, outputPrefix = "lobby", isLobby = true)
            Logging.IMP.info("[boot] Lobby assets downloaded [ok]")
        }
    }

    private fun processTestFlowIfNeeded() {
        if (isTestFlow()) {
            Logging.IMP.info("[boot] Processing testflow data [...]")
            getTestFlowName()?.let { testFlowName ->
                downloadAndExtractAssets(outputPrefix = "testflow", name = testFlowName)
                testFlowId = config?.getInt("testFlowId") ?: -1
                Logging.IMP.info("[boot] TestFlow ID: {}", testFlowId)
            }
        }
    }

    private fun downloadAndExtractAssets(
        gameType: GameType? = null,
        outputPrefix: String,
        name: String? = null,
        isLobby: Boolean = false
    ) {
        val start = System.currentTimeMillis()
        val url = buildAssetUrl(gameType, name, isLobby)
        val outputFile = "$outputPrefix$ZIP_EXTENSION"

        downloadFile(url, outputFile)
        unzip(outputFile, Paths.get("."))

        Logging.IMP.info("[boot] Assets fetched in ${System.currentTimeMillis() - start}ms")
    }

    private fun buildAssetUrl(
        gameType: GameType?,
        name: String?,
        isLobby: Boolean
    ): String {
        val baseUrl = "${serverConfig.apiBaseUrl}/retrieve?token=cock"
        return when {
            name != null -> "$baseUrl&name=$name"
            isLobby -> "$baseUrl&name=${gameType?.name}&lobby=true"
            else -> "$baseUrl&name=${gameType?.name}"
        }
    }

    private fun downloadFile(url: String, outputFile: String) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", serverConfig.authValue)
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofFile(Path.of(outputFile)))

        if (response.statusCode() != 200) {
            throw IOException("Failed to download file: ${response.statusCode()}")
        }
    }

    fun externalHost(): String {
        val url = URL("https://checkip.amazonaws.com")
        val input = BufferedReader(InputStreamReader(url.openStream()))

        return input.readLine()
    }

    private fun unzip(zipFilePath: String, destDir: Path) {
        ZipFile(zipFilePath).extractAll(destDir.toString())
        Files.delete(Path.of(zipFilePath))
    }
}