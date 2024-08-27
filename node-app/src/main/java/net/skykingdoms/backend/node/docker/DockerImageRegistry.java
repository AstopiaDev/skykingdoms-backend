package net.skykingdoms.backend.node.docker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum DockerImageRegistry {
    VELOCITY("docker.piby.eu/backend/velocity:1.0")
    ;

    String registry;

    public static final DockerImageRegistry[] VALUES = values();
}
