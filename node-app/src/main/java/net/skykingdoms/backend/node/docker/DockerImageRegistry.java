package net.skykingdoms.backend.node.docker;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.node.NodeApp;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum DockerImageRegistry {
    VELOCITY("docker.skykingdoms.net/backend/velocity:1.0")
    ;

    String registry;

    public String getRegistry() {
        return registry + "-" + NodeApp.INSTANCE.nodeConfiguration().getNetworkType().getPrefix();
    }

    public static final DockerImageRegistry[] VALUES = values();
}
