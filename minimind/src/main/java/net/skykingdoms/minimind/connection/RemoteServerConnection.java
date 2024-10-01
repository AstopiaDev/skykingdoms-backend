package net.skykingdoms.minimind.connection;

import io.netty.channel.socket.SocketChannel;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteServerChannel;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.PacketProtocol;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import net.skykingdoms.minimind.Minimind;

import java.util.EnumSet;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class RemoteServerConnection extends AbstractRemoteServerChannel {
    Minimind minimind;

    public RemoteServerConnection(final @NonNull Minimind minimind,
                                  final @NonNull LocalClientConnection local,
                                  final @NonNull SocketChannel channel) {
        super(local, channel);
        this.minimind = minimind;
    }

    @Override
    public void active() {
        EnumSet<Handshake.RequestFlag> flags = EnumSet.noneOf(Handshake.RequestFlag.class);
        if (minimind.isReconnecting()) {
            flags.add(Handshake.RequestFlag.IS_RECONNECT);
        }
        flags.add(Handshake.RequestFlag.HAS_FLUENT_DATA);

        sendPacket(
                new Handshake.Request(
                        RemoteConnectedType.SERVICE,
                        minimind.getConfiguration().getSecret(),
                        UUID.randomUUID(), // any value for SERVICE, doesn't matter
                        flags,
                        minimind.getServiceData(),
                0
                )
        );
    }

    @Override
    protected void onServerDisconnect() {
        minimind.setReconnecting(true);
    }

    @Override
    public void process(final @NonNull Handshake.Response packet,
                        final @NonNull ChannelProcessorContext ctx) {
        if (packet.getResult() == Handshake.Result.FAILED) {
            Logging.IMP.error("[HANDSHAKE] Unable to connect to coordinator: {}", packet.getMessage());
            return;
        }

        upgradeConnection(PacketProtocol.PLAY);
        minimind.setReconnecting(false);
    }
}