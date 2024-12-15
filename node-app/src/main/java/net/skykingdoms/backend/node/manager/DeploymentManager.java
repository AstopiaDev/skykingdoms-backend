package net.skykingdoms.backend.node.manager;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.type.DeploymentVersion;
import net.skykingdoms.backend.node.NodeApp;
import net.skykingdoms.backend.node.config.BaseNodeConfiguration;
import net.skykingdoms.backend.node.deploy.IInstanceDeployment;
import net.skykingdoms.backend.node.deploy.impl.BukkitInstanceDeployment;
import net.skykingdoms.backend.node.deploy.impl.ProxyInstanceDeployment;
import net.skykingdoms.backend.node.docker.DockerImageRegistry;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.packet.play.ServerDestroy;
import net.skykingdoms.backend.protocol.server.deploy.IDeployRequest;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manager that responds to managing coordinator's deployment requests
 * and handle this kind of shit :3
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum DeploymentManager {
    INSTANCE;

    private static final List<Integer> reservedPorts = new ArrayList<>();
    private static final ConcurrentHashMap<ServerDeploy.DeploymentTarget, IInstanceDeployment> deployments = new ConcurrentHashMap<>();

    /*
        ================
         DOCKER SECTION
        ================
     */

    DockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("unix:///var/run/docker.sock")
            .withDockerTlsVerify(false)
            .withRegistryUsername(System.getProperty("docker.user"))
            .withRegistryPassword(System.getProperty("docker.pass"))
            .withRegistryEmail(System.getProperty("docker.mail", "gorenjecalen@gmail.com"))
            .withRegistryUrl(System.getProperty("docker.registry", "http://127.0.0.1:5000/"))
            .build();

    DockerHttpClient dockerHttpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(dockerClientConfig.getDockerHost())
            .sslConfig(dockerClientConfig.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build();

    DockerClient client = DockerClientImpl.getInstance(dockerClientConfig, dockerHttpClient);

    /*
        END OF DOCKER SECTION
     */

    @NonFinal String externalHost;

    /**
     * Invoked once at bootstrap process to kill all
     */
    public void postBootstrap() {
        Logging.IMP.info("[Deploy/PB] Updating external address ...");
        Logging.IMP.info("[Deploy/PB] External address: {}", getExternalHost());
        Logging.IMP.info("[Deploy/PB] Loading deployment types ...");
        this.loadDeployments();

        BaseNodeConfiguration config = NodeApp.INSTANCE.nodeConfiguration();

        // Removing all containers, because we need only our shit :3
        client.listContainersCmd().withShowAll(true).exec().forEach(container -> {
            String containerName = container.getNames()[0]; // Get the container name
            if (containerName.startsWith("/" + config.getNetworkType().getPrefix() + "-")) {
                try {
                    client.killContainerCmd(container.getId()).exec();
                    client.removeContainerCmd(container.getId()).exec();
                    Logging.IMP.info("[Deploy/PB] Removed existent container: {}", container.getId());
                } catch (Exception e) {
                    Logging.IMP.error("[Deploy/PB] Failed to remove container: {}", container.getId(), e);
                }
            }
        });

        // Cleaning up images, pulling new from repository
        try {
            for (DockerImageRegistry registry : DockerImageRegistry.VALUES) {
                client.removeImageCmd(registry.getRegistry()).withForce(true).exec();
                client.pullImageCmd(registry.getRegistry()).start().awaitCompletion();
                Logging.IMP.info("[Deploy/PB] Pulled image: {}", registry.name());
            }

            for (DeploymentVersion version : DeploymentVersion.VALUES) {
                String image = version.image() + "-" + NodeApp.INSTANCE.nodeConfiguration().getNetworkType().getPrefix();
                try {
                    client.removeImageCmd(image).withForce(true).exec();
                } catch (Exception e) {
                    // do nothing
                }
                client.pullImageCmd(image).start().awaitCompletion();
                Logging.IMP.info("[Deploy/PB] Pulled image: {}", version.name());
            }

            Logging.IMP.info("[Deploy/PB] Docker Image cleanup complete");
        } catch (InterruptedException exception) {
            Logging.IMP.error("[Deploy/PB] Couldn't fetch images from registry");
        }
    }

    private void loadDeployments() {
        deployments.put(ServerDeploy.DeploymentTarget.PROXY, new ProxyInstanceDeployment(client));
        deployments.put(ServerDeploy.DeploymentTarget.BUKKIT, new BukkitInstanceDeployment(client));
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

        final int port = getAvailablePort();
        reservedPorts.add(port);

        NodeApp.INSTANCE.scheduler().schedule(() -> {
            reservedPorts.remove(port);
        }, 10, TimeUnit.SECONDS);

        final ArrayList<PortBinding> portBindings = new ArrayList<PortBinding>();
        portBindings.add(PortBinding.parse(port + ":" + port));
        if (debugPort > 0) {
            portBindings.add(PortBinding.parse(debugPort + ":" + debugPort));
        }

        final HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings);
                //.withAutoRemove(testFlowId <= 0);

        String containerId = deployments.get(deploymentTarget).handle(request, hostConfig, port,
                testFlow ? testFlowName : null, debugPort, testFlowId);

        Logging.IMP.info("[Deploy] T: {}, T-NAME: {}, T-ID: {}", testFlow, testFlowName != null ? testFlowName : "none", testFlowId);

        client.renameContainerCmd(containerId).withName(request.getTransactionId().toString()).exec();
        client.startContainerCmd(containerId).exec();

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
            client.stopContainerCmd(containerName).exec();
            client.removeContainerCmd(containerName).exec();
            Logging.IMP.info("[Deploy] Stopped container {} gracefully", containerName);
        } else {
            client.killContainerCmd(containerName).exec();
            client.removeContainerCmd(containerName).exec();
            Logging.IMP.info("[Deploy] Stopped container {} non-gracefully", containerName);
        }

        ctx.setResponse(new ServerDestroy.Response(true));
    }

    private boolean available(int port) throws IllegalStateException {
        if (DeploymentManager.INSTANCE.reservedPorts.contains(port)) {
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

    private int getAvailablePort() {
        int port = 35565;
        while (port <= 65535) {
            if (available(port)) {
                return port;
            }
            port++;
        }
        throw new IllegalStateException("No available ports in the range");
    }

    @SneakyThrows
    public String getExternalHost() {
        if (externalHost == null) {
            val url = new URL("https://checkip.amazonaws.com");
            val in = new BufferedReader(new InputStreamReader(
                    url.openStream()));

            externalHost = in.readLine();
        }

        return externalHost;
    }
}