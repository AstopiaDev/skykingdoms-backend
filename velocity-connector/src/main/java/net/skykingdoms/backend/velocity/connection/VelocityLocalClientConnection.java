package net.skykingdoms.backend.velocity.connection;

import io.netty.channel.socket.SocketChannel;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.backend.protocol.channel.AbstractClientChannel;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteServerChannel;
import net.skykingdoms.backend.velocity.gamer.Gamer;

import java.net.SocketAddress;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class VelocityLocalClientConnection extends AbstractClientChannel {
    public VelocityLocalClientConnection(final @NonNull SocketAddress address, final int threads) {
        super(address, threads);
    }

    @Override
    protected AbstractRemoteServerChannel newServerChannel(SocketChannel channel) {
        return new RemoteServerConnection(channel);
    }

    @Override
    public void reconnect() {
        if (!reconnecting) {
            for (val gamer : Gamer.getGamers()) {
// annoying asf
                gamer.sendLocalizedMessage(gamer.isModern(), "core_reconnect_message");
            }
        }

        super.reconnect();
    }

    @Override
    protected void doReconnect() {
// annoying asf
//        for (val gamer : Gamer.getPlayers()) {
//            gamer.sendLocalizedTitle(
//                    "core_reconnect_process_title",
//                    "core_reconnect_process_subtitle"
//            );
//        }

        super.doReconnect();
    }

}
