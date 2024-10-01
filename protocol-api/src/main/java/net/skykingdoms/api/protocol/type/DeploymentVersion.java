package net.skykingdoms.api.protocol.type;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public enum DeploymentVersion {
    V1_8_8("docker.skykingdoms.net/backend/bukkit:1.8.8"),
    V1_20_4("docker.skykingdoms.net/backend/bukkit:1.20.4"),
    ;

    String image;

    public final static DeploymentVersion[] VALUES = values();
}
