package net.skykingdoms.minimind.connection;

import io.netty.channel.socket.SocketChannel;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.channel.AbstractClientChannel;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteServerChannel;
import net.skykingdoms.minimind.Minimind;

import java.net.SocketAddress;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class LocalClientConnection extends AbstractClientChannel {
    Minimind minimind;

    public LocalClientConnection(final @NonNull Minimind minimind,
                                 final @NonNull SocketAddress address,
                                 final int threads) {
        super(address, threads);
        this.minimind = minimind;
    }

    @Override
    protected AbstractRemoteServerChannel newServerChannel(SocketChannel channel) {
        return new RemoteServerConnection(minimind, this, channel);
    }

}