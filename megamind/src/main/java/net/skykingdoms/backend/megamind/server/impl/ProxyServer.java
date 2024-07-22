package net.skykingdoms.backend.megamind.server.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;
import net.skykingdoms.backend.protocol.server.IDeployable;

import java.net.InetSocketAddress;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class ProxyServer extends AbstractPlayerHolder implements IDeployable {

    NodeServer node;

    /**
     * Remote proxy's host
     */
    InetSocketAddress host;

    public ProxyServer(final @NonNull String name,
                       final @NonNull NodeServer server,
                       final @NonNull RemoteClientConnection connection,
                       final @NonNull InetSocketAddress remote) {
        super(name, connection);
        this.node = server;
        this.host = remote;
    }

    // TODO: Proxy geo position method...

}
