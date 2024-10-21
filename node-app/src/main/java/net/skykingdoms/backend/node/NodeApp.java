package net.skykingdoms.backend.node;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import net.skykingdoms.api.backend.util.config.Configuration;
import net.skykingdoms.api.backend.util.config.ConfigurationProvider;
import net.skykingdoms.api.backend.util.config.YamlConfiguration;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.NetworkType;
import net.skykingdoms.backend.node.config.BaseNodeConfiguration;
import net.skykingdoms.backend.node.connection.LocalClientConnection;
import net.skykingdoms.backend.node.connection.NodeConnection;
import net.skykingdoms.backend.node.manager.DeploymentManager;
import net.skykingdoms.backend.protocol.exception.ConnectException;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentNodeDataImpl;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Getter
public enum NodeApp {
    INSTANCE;

    // General purpose system fields
    @NonFinal Configuration yamlConfiguration;
    @Getter @NonFinal BaseNodeConfiguration nodeConfiguration;
    @Getter final long startDate = System.currentTimeMillis();
    @Getter @NonFinal long bootElapsedEpoch;

    // Executors
    @Getter @NonFinal ExecutorService executor;
    @Getter @NonFinal ScheduledExecutorService scheduler;

    // Connection related shit
    @Getter @Setter @NonFinal boolean reconnecting = false;
    @NonFinal InetSocketAddress address;
    @Getter @NonFinal LocalClientConnection connection;

    // Persistent node data, that is being updated by server
    @Getter @Setter @NonFinal FluentNodeDataImpl nodeData;

    // current network type
    @NonFinal NetworkType networkType;

    public void init() {
        this.loadConfig();
        this.initExecutors();
        this.bootManagers();
        this.connect();

        this.bootElapsedEpoch = System.currentTimeMillis() - startDate;
        Logging.IMP.info("[Bootstrap] Done node initialization process. {} ms elapsed", bootElapsedEpoch);
    }

    private void loadConfig() {
        reloadConfiguration();
        networkType = NetworkType.valueOf(System.getProperty("network.type", NetworkType.DEVELOPMENT.name()));
        Logging.IMP.info("Network type for this coordinator instance is: {}", networkType.name());
    }

    private void initExecutors() {
        scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        executor = Executors.newCachedThreadPool();
        Logging.IMP.info("[Bootstrap] Executors instantiated");
    }

    private void bootManagers() {
        DeploymentManager.INSTANCE.postBootstrap();
    }

    private void connect() {
        this.address = new InetSocketAddress(
                nodeConfiguration.getRemoteCoordinatorHost(),
                nodeConfiguration.getRemoteCoordinatorPort()
        );

        this.connection = new LocalClientConnection(address, 5345);

        try {
            connection.connectSynchronized();
            Logging.IMP.info("[Bootstrap] Synchronized connection invoked");
        } catch (ConnectException e) {
            e.printStackTrace();
            Logging.IMP.warn("[Bootstrap] Shutting down due to internal connection error");
            return;
        }

        NodeConnection.setConnection(connection);
    }

    public void reloadConfiguration() {
        val provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        val configFile = new File("config.yml");

        try {
            if (!configFile.exists()) {
                Configuration defaultConfiguration = new Configuration();
                defaultConfiguration.set("allowDockerEnvironmentVars", true);
                defaultConfiguration.set("localBindHost", "127.0.0.1");
                defaultConfiguration.set("localBindPort", -1);
                defaultConfiguration.set("coordinatorHost", "");
                defaultConfiguration.set("coordinatorPort", -1);
                defaultConfiguration.set("coordinatorSecret", "");

                //noinspection ResultOfMethodCallIgnored
                configFile.createNewFile();

                provider.save(defaultConfiguration, configFile);
            }

            yamlConfiguration = provider.load(configFile);

            nodeConfiguration = new BaseNodeConfiguration(
                    yamlConfiguration.getString("nodeName"),
                    yamlConfiguration.getString("localBindHost"),
                    yamlConfiguration.getInt("localBindPort"),
                    yamlConfiguration.getBoolean("allowDockerEnvironmentVars"),
                    yamlConfiguration.getString("coordinatorHost"),
                    yamlConfiguration.getInt("coordinatorPort"),
                    yamlConfiguration.getString("coordinatorSecret"),
                    NetworkType.valueOf(System.getProperty("network.type", NetworkType.DEVELOPMENT.name())),
                    yamlConfiguration.getStringList("scaleTypes").stream()
                            .map(ServerScaleType::valueOf).distinct().toArray(ServerScaleType[]::new)
            );
        } catch (IOException e) {
            Logging.IMP.stacktrace(e, "Can't load configuration file");
        }
    }

    public void reloadNodeData() {
        int allocatedRamMB = (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
        int maxRamMB = (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));

        if (nodeData == null) {
            nodeData = new FluentNodeDataImpl("", nodeConfiguration.getScaleTypes(), allocatedRamMB, maxRamMB);
        } else {
            nodeData.setScaleTypes(nodeConfiguration.getScaleTypes());
            nodeData.setAllocatedRamMB(allocatedRamMB);
            nodeData.setMaxRamMB(maxRamMB);
        }
    }
}
