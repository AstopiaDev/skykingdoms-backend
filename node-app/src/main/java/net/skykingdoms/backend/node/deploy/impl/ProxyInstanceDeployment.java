package net.skykingdoms.backend.node.deploy.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import lombok.NonNull;
import net.skykingdoms.backend.node.NodeApp;
import net.skykingdoms.backend.node.deploy.InstanceDeploymentImpl;
import net.skykingdoms.backend.node.docker.DockerImageRegistry;
import net.skykingdoms.backend.node.manager.DeploymentManager;
import net.skykingdoms.backend.protocol.server.deploy.IDeployRequest;
import org.jetbrains.annotations.Nullable;

public class ProxyInstanceDeployment extends InstanceDeploymentImpl {
    public ProxyInstanceDeployment(DockerClient client) {
        super(client);
    }

    @Override
    public String handle(@NonNull IDeployRequest request, @NonNull HostConfig config, int port, @Nullable String testFlowName, int debugPort, int testFlowId) {
        return client.createContainerCmd(DockerImageRegistry.VELOCITY.getRegistry())
                .withEnv(
                        "CORD_SECRET=" + NodeApp.INSTANCE.nodeConfiguration().getSecret(),
                        "CORD_TID=" + request.getTransactionId(),
                        "PROXY_HOST=" + "0.0.0.0:" + port,
                        "EXTERNAL_ADDR=" + DeploymentManager.INSTANCE.getExternalHost()
                )
                .withHostConfig(config)
                .withExposedPorts(ExposedPort.tcp(port))
                .exec().getId();
    }
}
