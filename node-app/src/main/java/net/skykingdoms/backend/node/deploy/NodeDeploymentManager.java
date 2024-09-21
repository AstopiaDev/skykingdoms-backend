package net.skykingdoms.backend.node.deploy;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import net.skykingdoms.backend.node.NodeApp;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.packet.play.ServerDestroy;
import net.skykingdoms.backend.api.protocol.type.GameMode;
import net.skykingdoms.backend.protocol.server.deploy.IDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.ProxyDeployRequest;
import net.skykingdoms.backend.api.protocol.type.DeploymentVersion;
import net.skykingdoms.backend.api.protocol.util.Consumer;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manager that responds to managing coordinator's deployment requests
 * and handle this kind of shit :3
 *
 *
 */
public enum NodeDeploymentManager {
    INSTANCE;

    final DockerClientConfig cockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("npipe:////./pipe/podman-machine-default")
            .withDockerTlsVerify(false)
            .withRegistryUsername(System.getProperty("docker.user"))
            .withRegistryPassword(System.getProperty("docker.pass"))
            .withRegistryEmail(System.getProperty("docker.mail", "gorenjecalen@gmail.com"))
            .withRegistryUrl(System.getProperty("docker.registry", "http://127.0.0.1:5000/"))
            .build();

    final DockerHttpClient cockerHttpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(cockerConfig.getDockerHost())
            .sslConfig(cockerConfig.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

    final DockerClient client = DockerClientImpl.getInstance(cockerConfig, cockerHttpClient);

    private final List<Integer> reservedPorts = new ArrayList<>();

    private static final String VELOCITY_IMAGE_NAME = "docker.piby.eu/backend/velocity:1.0";

    private static String externalHost;

    List<Consumer<Void>> queue = new ArrayList<>();

    @Getter
    boolean loaded = false;

    public void addQueue(Consumer<Void> consumer) {
        queue.add(consumer);
    }

    @SneakyThrows
    public static String getExternalHost() {
        if (externalHost == null) {
            val url = new URL("https://checkip.amazonaws.com");
            val in = new BufferedReader(new InputStreamReader(
                    url.openStream()));

            externalHost = in.readLine();
        }

        return externalHost;
    }

    /**
     * Invoked once at bootstrap process to kill all
     */
    public void postBootstrap() {
        Logging.IMP.info("[Deploy/PB] Updating external address ...");
        Logging.IMP.info("[Deploy/PB] External address: {}", getExternalHost());

        // Removing all containers, because we need only our shit :3
        client.listContainersCmd().withShowAll(true).exec().forEach(container -> {
            if (container.getState().equals("running")) {
                String containerName = container.getNames()[0]; // Get the container name
                if (!containerName.equals("/registry") && !containerName.equals("/node-app") && !containerName.equals("/portainer_agent")
                && GameMode.valueOf(containerName.substring(1).toUpperCase(), null) == null) {
                    try {
                        client.killContainerCmd(container.getId()).exec();
                        client.removeContainerCmd(container.getId()).exec();
                        Logging.IMP.info("[Deploy/PB] Removed existent container: {}", container.getId());
                    } catch (Exception e) {
                        Logging.IMP.error("[Deploy/PB] Failed to remove container: {}", container.getId(), e);
                    }
                }
            }
        });



        // Cleaning up images, pulling new from repository
        try {
            client.removeImageCmd(VELOCITY_IMAGE_NAME).withForce(true).exec();

            for (DeploymentVersion version : DeploymentVersion.VALUES) {
                client.removeImageCmd(version.image()).withForce(true).exec();
            }
        } catch (Exception e) { // images not found, don't care
        }

        Logging.IMP.info("[Deploy/PB] Removed old images... Pulling new shit");

        for (String ye : cockerConfig.getAuthConfigurations().getConfigs().keySet()) {
            Logging.IMP.info(ye);
        }

        try {
            client.pullImageCmd(VELOCITY_IMAGE_NAME).start().awaitCompletion();
            Logging.IMP.info("[Deploy/PB] Pulled proxy image!");

            for (DeploymentVersion version : DeploymentVersion.VALUES) {
                client.pullImageCmd(version.image()).start().awaitCompletion();
                Logging.IMP.info("[Deploy/PB] Pulled " + version.image() + " image!");
            }

            loaded = true;
            for (Consumer<Void> consumer : queue) {
                consumer.accept(null);
            }
        } catch (InterruptedException exception) {
            Logging.IMP.error("[Deploy/PB] Couldn't fetch images from remote");
        }
    }

    private String _handleProxyDeploy(final @NonNull ProxyDeployRequest request,
                                      final @NonNull HostConfig config,
                                      final int port) {
        return client.createContainerCmd(VELOCITY_IMAGE_NAME)
                .withEnv(
                        "CORD_SECRET=" + NodeApp.INSTANCE.nodeConfiguration().getSecret(),
                        "CORD_TID=" + request.getTransactionId(),
                        "PROXY_HOST=" + "0.0.0.0:" + port,
                        "EXTERNAL_ADDR=" + getExternalHost()
                )
                .withHostConfig(config)
                .withExposedPorts(ExposedPort.tcp(port))
                .exec().getId();
    }

    private String _handleBukkitDeploy(final @NonNull BukkitDeployRequest request,
                                       final @NonNull HostConfig config,
                                       final int port,
                                       final @Nullable String testFlowName,
                                       final int debugPort,
                                       final int testFlowId) {
        val data = request.getData();
        val envVars = Lists.newArrayList(
                "CORD_SECRET=" + NodeApp.INSTANCE.nodeConfiguration().getSecret(),
                "CORD_TID=" + request.getTransactionId(),
                "BUKKIT_HOST=" + "0.0.0.0:" + port,
                "SERVER_SCALE=" + data.getServerScale().name(),
                "EXTERNAL_ADDR=" + getExternalHost(),
                "GAME_TYPE=" + data.getGameType());

        if (data.getGameMode() != null) {
            envVars.add("GAME_MODE=" + data.getGameMode().name());
        }

        if (testFlowName != null) {
            envVars.add("TEST_FLOW=" + testFlowName);
            envVars.add("TEST_FLOW_ID=" + testFlowId);
        }

        return client.createContainerCmd(data.getGameType().getVersion().image())
                .withTty(true)
                .withEnv(envVars)
                .withHostConfig(config)
                .withExposedPorts(ExposedPort.tcp(port), ExposedPort.tcp(debugPort))
                .exec().getId();
    }

    /**
     * Invoked right from remote server connection handler,
     * when the request packet is received.
     */
    public void handleRequest(
            final @NonNull ServerDeploy.DeploymentTarget deploymentTarget,
            final @NonNull IDeployRequest request,
            final boolean testFlow,
            final @Nullable String testFlowName,
            final int debugPort,
            final int testFlowId) {

        val port = getAvailablePort();
        reservedPorts.add(port);

        NodeApp.INSTANCE.scheduler().schedule(() -> {
            reservedPorts.remove(port);
        }, 10, TimeUnit.SECONDS);

        val portBindings = new ArrayList<PortBinding>();
        //Logging.IMP.info("cock 1");
        portBindings.add(PortBinding.parse(port + ":" + port));
        if (debugPort > 0) {
            portBindings.add(PortBinding.parse(debugPort + ":" + debugPort));
        }

        val hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings)
        // TODO: YES PLS NEED TOA DD THIS BACK TESTING SON ONON NOAN OFNDSOFNOSDOFSNDOF
               .withAutoRemove(testFlowId <= 0);
      //  Logging.IMP.info("cock 2");

        String containerId;
        if (deploymentTarget == ServerDeploy.DeploymentTarget.PROXY) {
            containerId = _handleProxyDeploy((ProxyDeployRequest) request, hostConfig, port);
        } else { // bukkit server
            containerId = _handleBukkitDeploy((BukkitDeployRequest) request, hostConfig, port,
                    testFlow ? testFlowName : null, debugPort, testFlowId);
        }
       // Logging.IMP.info("cock 3 " + containerId);

        //Logging.IMP.info("Starting the container...");
        try {
            client.startContainerCmd(containerId).exec();
            client.renameContainerCmd(containerId).withName(request.getTransactionId().toString()).exec();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Logging.IMP.info("[Deploy] Target: {} @ TID: {} @ CID: {} @ PORT: {} @ TF: {} @ AR: {}",
                deploymentTarget,
                request.getTransactionId(),
                containerId,
                port,
                testFlowId,
                hostConfig.getAutoRemove());
    }

    /**
     * Invoked once per each container instance, basically we update each container name
     * from dynamic (transaction id) to static (servername)
     *
     * @param transactionId transaction ID
     * @param newName       new container name
     */
    public void handleContainerNameUpdate(final @NonNull UUID transactionId,
                                          final @NonNull String newName) {
        Logging.IMP.info("[Deploy] Received container data update packet: {} -> {}",
                transactionId.toString(),
                newName);

        client.renameContainerCmd(transactionId.toString()).withName(newName).exec();
    }

    /**
     * Basically destroys a specified container.
     *
     * @param graceful      should we shut down container gracefully of just stop and remove it
     * @param containerName name of the server instance
     */
    public void handleDestructionRequest(final @NonNull String containerName,
                                         final boolean graceful,
                                         final ChannelProcessorContext ctx) {

        client.attachContainerCmd(containerName).exec(new ResultCallback<Frame>() {
            @Override
            public void onStart(Closeable closeable) { }

            @Override
            public void onNext(Frame object) { }

            @Override
            public void onError(Throwable throwable) {
                ctx.setResponse(new ServerDestroy.Response(false));
            }

            @Override
            public void onComplete() { }

            @Override
            public void close() { }
        });

        if (graceful) {
            client.stopContainerCmd(containerName).exec(); // yeh ahhhhhh
            client.removeContainerCmd(containerName).exec();
            Logging.IMP.info("[Deploy] Stopped container {} gracefully", containerName);
        } else {
            client.killContainerCmd(containerName).exec();
            client.removeContainerCmd(containerName).exec();
            Logging.IMP.info("[Deploy] Stopped container {} non-gracefully", containerName);
        }

        ctx.setResponse(new ServerDestroy.Response(true));
    }

/*    private int getAvailablePort() {
        int ret = 25565;
        while (!available(ret)) {
            ret += 1;
        }
        return ret;
    }*/

    private static boolean available(int port) throws IllegalStateException {
        if (NodeDeploymentManager.INSTANCE.reservedPorts.contains(port)) {
            return false;
        }
        try (val ignored = new Socket("0.0.0.0", port)) {
            return false;
        } catch (ConnectException e) {
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Error while trying to check open port", e);
        }
    }

    private static int getAvailablePort() {
        int port = 35565;
        while (port <= 65535) {
            if (available(port)) {
                return port;
            }
            port++;
        }
        throw new IllegalStateException("No available ports in the range");
    }

}