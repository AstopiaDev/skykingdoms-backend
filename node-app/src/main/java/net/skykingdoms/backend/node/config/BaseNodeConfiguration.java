package net.skykingdoms.backend.node.config;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.NetworkType;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@EqualsAndHashCode
@Data
public final class BaseNodeConfiguration {

    /**
     * Unique node name
     */
    String nodeName;

    /**
     * Local connection address
     */
    String localBindAddressHost;
    int localBindAddressPort;

    /**
     * Docker env vars
     */
    boolean allowCockerShit;

    /**
     * Address of remote coordinator service instance
     */
    String remoteCoordinatorHost;
    int remoteCoordinatorPort;
    String secret;
    NetworkType networkType;
    ServerScaleType[] scaleTypes;
}