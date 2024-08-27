package net.skykingdoms.backend.node.deploy;

import com.github.dockerjava.api.model.HostConfig;
import lombok.NonNull;
import net.skykingdoms.backend.protocol.server.deploy.IDeployRequest;
import org.jetbrains.annotations.Nullable;

public interface IInstanceDeployment {
    String handle(final @NonNull IDeployRequest request,
                  final @NonNull HostConfig config,
                  final int port,
                  final @Nullable String testFlowName,
                  final int debugPort,
                  final int testFlowId);

    default String handle(final @NonNull IDeployRequest request,
                          final @NonNull HostConfig config,
                          final int port) { return handle(request, config, port, null, -1, -1); }
}
