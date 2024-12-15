package net.skykingdoms.backend.bukkit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.backend.util.config.Configuration;
import net.skykingdoms.api.backend.util.config.ConfigurationProvider;
import net.skykingdoms.api.backend.util.config.YamlConfiguration;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.api.protocol.util.TextFormat;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.bukkit.boot.BukkitConnectorBoot;
import net.skykingdoms.backend.bukkit.connection.BukkitLocalClientConnection;
import net.skykingdoms.backend.bukkit.connection.Connection;
import net.skykingdoms.backend.bukkit.security.BukkitSecurityManager;
import net.skykingdoms.backend.protocol.exception.ConnectException;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
public enum BukkitConnector {
    INSTANCE;

    BukkitLocalClientConnection connection;
    FluentBukkitDataImpl fluentBukkitData;
    ServerScaleType serverScaleType;
    @Setter boolean reconnecting = false;
    Configuration configuration;
    BukkitConnectorBoot plugin;
    SocketAddress address;
    UUID transactionId;
    GameType gameType;
    GameMode gameMode;
    int testFlowId;
    String secret;

    public void construct(BukkitConnectorBoot plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();
        this.loadConfiguration();

        // props
        this.gameMode = GameMode.valueOf(System.getProperty("gameMode").toUpperCase(), null);
        this.gameType = GameType.valueOf(System.getProperty("gameType").toUpperCase());
        this.serverScaleType = ServerScaleType.valueOf(System.getProperty("serverScale").toUpperCase());

        // env vars
        this.secret = configuration == null ? System.getenv("CORD_SECRET") : configuration.getString("CORD_SECRET");
        this.transactionId = configuration == null ? UUID.fromString(System.getenv("CORD_TID")) : UUID.fromString(configuration.getString("CORD_TID"));
        this.testFlowId = Integer.parseInt(System.getProperty("testFlowId", "-1"));

        Logging.IMP.info("[TestFlow] -> {}", testFlowId);

        this.address = new InetSocketAddress(
                plugin.getConfig().getString("megamind-ip"),
                plugin.getConfig().getInt("megamind-port")
        );
    }

    public void loadConfiguration() {
        val provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        val configFile = new File("config.yml");

        if (configFile.exists()) {
            try {
                configuration = provider.load(configFile);
             //   secret = configuration.getString("secret");
            } catch (IOException e) {
                Logging.IMP.stacktrace(e, "Can't load configuration file");
            }
        }
    }

    public void init() {
        this.connection = new BukkitLocalClientConnection(address, 2);

        // we init this shit for handshake cocked stuff
        Logging.IMP.info("S: {} | G: {} | EX: {} | P: {}", serverScaleType, gameType, System.getProperty("externalAddr"), bukkitAddress().getPort());
        this.fluentBukkitData = new FluentBukkitDataImpl(gameMode != null ? gameMode.name().toLowerCase() : "N/A", "N/A", serverScaleType, gameType,
                System.getProperty("externalAddr"), bukkitAddress().getPort());

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                this.connection.connectSynchronized();
                Logging.IMP.info("Connection synchronized!");
            } catch (ConnectException e) {
                e.printStackTrace();
            }
        });

        BukkitSecurityManager.INSTANCE.postBootstrap();

        Connection.setConnection(connection);
    }

    public void load() {
        Language.reloadLanguages();
    }

    public void shutdown() {
        for (var player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer(TextFormat.formatText("Server {} shut down at {}",
                    fluentBukkitData.getServerName(), fluentBukkitData.getNodeName()));
        }
    }

    public @NotNull InetSocketAddress bukkitAddress() {
        val env = configuration == null ? System.getenv("BUKKIT_HOST").split(":") : configuration.getString("BUKKIT_HOST").split(":"); // todo: refactor. shitcode
        return InetSocketAddress.createUnresolved(env[0], Integer.parseInt(env[1]));
    }
}
