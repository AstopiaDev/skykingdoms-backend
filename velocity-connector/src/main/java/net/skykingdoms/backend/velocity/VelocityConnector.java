package net.skykingdoms.backend.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github._4drian3d.authmevelocity.api.velocity.AuthMeVelocityAPI;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.luckperms.rest.LuckPermsRestClient;
import net.skykingdoms.backend.base.data.state.NetworkState;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.permission.RankSqlHandler;
import net.skykingdoms.backend.base.util.config.Configuration;
import net.skykingdoms.backend.base.util.config.ConfigurationProvider;
import net.skykingdoms.backend.base.util.config.YamlConfiguration;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentProxyDataImpl;
import net.skykingdoms.backend.velocity.boot.VelocityConnectorBoot;
import net.skykingdoms.backend.velocity.connection.Connection;
import net.skykingdoms.backend.velocity.connection.VelocityLocalClientConnection;
import net.skykingdoms.backend.velocity.listener.KickListener;
import net.skykingdoms.backend.velocity.listener.PingListener;
import net.skykingdoms.backend.velocity.listener.PlayerListener;
import net.skykingdoms.backend.velocity.listener.TabCompleteListener;
import net.skykingdoms.backend.velocity.security.BungeeSecurityManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
public enum VelocityConnector {
    INSTANCE;

    VelocityConnectorBoot boot;
    ProxyServer server;
    VelocityLocalClientConnection connection;
    @Setter FluentProxyDataImpl proxyData;
    final AtomicBoolean initialized = new AtomicBoolean(false);
    final AtomicBoolean reconnecting = new AtomicBoolean(false);
    AuthMeVelocityAPI authMeAPI;
    @Setter NetworkState networkState = NetworkState.OPEN;
    Configuration configuration;

    public void construct(VelocityConnectorBoot boot, ProxyServer proxyServer) {
        this.boot = boot;
        this.server = proxyServer;

        try {
            Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void init() {
        initialized.set(true);

        try {
            this.authMeAPI = (AuthMeVelocityAPI) server.getPluginManager()
                    .getPlugin("authmevelocity")
                    .get().getInstance().get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.reloadConfiguration();

        RankSqlHandler.INSTANCE.setLuckPerms(
                LuckPermsRestClient.builder()
                        .baseUrl(configuration.getString("luckperms-url"))
                        .apiKey(configuration.getString("luckperms-api-key"))
                        .build()
        );
        this.connection = new VelocityLocalClientConnection(
                new InetSocketAddress(configuration.getString("coordinatorHost"), configuration.getInt("coordinatorPort")),
                2
        );

        this.proxyData = new FluentProxyDataImpl("N/A", "N/A",
                System.getenv("EXTERNAL_ADDR"),
                Integer.parseInt(System.getenv("PROXY_HOST").split(":")[1])
        );

        Logging.IMP.info("External address: {}", this.proxyData.getHost());
        Language.reloadLanguages();


        connection.connectSynchronized();
        Connection.setConnection(connection);

        server.getEventManager().register(boot, new PlayerListener());
        server.getEventManager().register(boot, new PingListener());
        server.getEventManager().register(boot, new TabCompleteListener());
        server.getEventManager().register(boot, new KickListener());

/*
        BungeeSecurityManager.INSTANCE.postBootstrap();
*/
    }

    public void reloadConfiguration() {
        val provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        val configFile = new File("proxy-config.yml");

        try {
            if (!configFile.exists()) {
                Configuration defaultConfiguration = new Configuration();
                defaultConfiguration.set("coordinatorHost", "");
                defaultConfiguration.set("coordinatorPort", -1);

                //noinspection ResultOfMethodCallIgnored
                configFile.createNewFile();

                provider.save(defaultConfiguration, configFile);
            }

            configuration = provider.load(configFile);
        } catch (IOException e) {
            Logging.IMP.stacktrace(e, "Can't load configuration file");
        }
    }

    public int getGlobalOnline() {
        return server.getPlayerCount();
    }
}
