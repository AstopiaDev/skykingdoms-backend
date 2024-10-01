package net.skykingdoms.backend.node.deploy.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.node.NodeApp;
import net.skykingdoms.backend.node.deploy.InstanceDeploymentImpl;
import net.skykingdoms.backend.node.manager.DeploymentManager;
import net.skykingdoms.backend.protocol.server.deploy.IDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import org.jetbrains.annotations.Nullable;

public class BukkitInstanceDeployment extends InstanceDeploymentImpl {
    public BukkitInstanceDeployment(DockerClient client) {
        super(client);
    }

    @Override
    public String handle(@NonNull IDeployRequest request, @NonNull HostConfig config, int port, @Nullable String testFlowName, int debugPort, int testFlowId) {
        BukkitDeployRequest bukkitRequest = (BukkitDeployRequest) request;
        val data = bukkitRequest.getData();
        val envVars = Lists.newArrayList(
                "CORD_SECRET=" + NodeApp.INSTANCE.nodeConfiguration().getSecret(),
                "CORD_TID=" + request.getTransactionId(),
                "BUKKIT_HOST=" + "0.0.0.0:" + port,
                "SERVER_SCALE=" + data.getServerScale().name(),
                "EXTERNAL_ADDR=" + DeploymentManager.INSTANCE.getExternalHost(),
                "GAME_TYPE=" + data.getGameType());

        if (data.getGameMode() != null) {
            envVars.add("GAME_MODE=" + data.getGameMode().name());
        }

        if (testFlowName != null) {
            envVars.add("TEST_FLOW=" + testFlowName);
            envVars.add("TEST_FLOW_ID=" + testFlowId);
        }

        return client.createContainerCmd(data.getGameType().getVersion().image() + "-" + NodeApp.INSTANCE.nodeConfiguration().getNetworkType().getPrefix())
                .withTty(true)
                .withEnv(envVars)
                .withHostConfig(config)
                .withExposedPorts(ExposedPort.tcp(port), ExposedPort.tcp(debugPort))
                .exec().getId();
    }
}
