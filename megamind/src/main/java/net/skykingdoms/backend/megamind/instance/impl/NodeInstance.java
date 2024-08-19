package net.skykingdoms.backend.megamind.instance.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.instance.AbstractRemoteConnectable;
import net.skykingdoms.backend.protocol.server.INodeServer;
import net.skykingdoms.backend.protocol.server.ServerScaleType;

/**
 * Represents connection to remote NODE driver.
 * @Author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class NodeInstance extends AbstractRemoteConnectable implements INodeServer {

    ServerScaleType[] scaleTypes;
    @NonFinal @Setter int allocatedRamMB;
    int maxRamMB;

    public NodeInstance(final @NonNull String name,
                        final @NonNull RemoteClientConnection connection,
                        final @NonNull ServerScaleType[] scaleTypes,
                        final int allocatedRamMB,
                        final int maxRamMB) {
        super(name, connection);
        this.scaleTypes = scaleTypes;
        this.allocatedRamMB = allocatedRamMB;
        this.maxRamMB = maxRamMB;
    }

}
