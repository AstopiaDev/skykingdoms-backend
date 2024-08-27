package net.skykingdoms.backend.node.deploy;

import com.github.dockerjava.api.DockerClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public abstract class InstanceDeploymentImpl implements IInstanceDeployment {
    DockerClient client;
}
