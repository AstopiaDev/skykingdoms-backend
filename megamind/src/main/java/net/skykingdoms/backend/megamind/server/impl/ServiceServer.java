package net.skykingdoms.backend.megamind.server.impl;

import lombok.NonNull;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.server.AbstractRemoteConnectable;
import net.skykingdoms.backend.protocol.server.IConnectable;

public class ServiceServer extends AbstractRemoteConnectable implements IConnectable {
    public ServiceServer(final @NonNull String name,
                      final @NonNull RemoteClientConnection connection) {
        super(name, connection);
    }
}