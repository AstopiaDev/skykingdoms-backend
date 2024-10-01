package net.skykingdoms.backend.protocol.server.deploy.impl;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.server.deploy.AbstractDeployRequest;

import java.util.UUID;

/**
 * A request can be headed as Lobby, some special shiet or a GameServer
 *
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public final class PersistentBukkitDeployRequest extends AbstractDeployRequest {

    /**
     * Server scale type
     */
    ScalableDataImpl data;
    String name;
    String address;
    int port;

    public PersistentBukkitDeployRequest(final @NonNull UUID transactionId,
                               final @NonNull ScalableDataImpl data,
                                         final @NonNull String name,
                                         final @NonNull String address, final int port) {
        this.transactionId = transactionId;
        this.data = data;
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public PersistentBukkitDeployRequest(final @NonNull ScalableDataImpl data) {
        this(UUID.randomUUID(), data, "",  "", 0);
    }

    @Override
    public AbstractDeployRequest serialize(@NonNull ByteBuf out) {
        super.serialize(out);
        data.serialize(out);
        PacketUtils.writeString(out, name);
        PacketUtils.writeString(out, address);
        out.writeInt(port);
        return this;
    }

    @Override
    public AbstractDeployRequest deserialize(@NonNull ByteBuf in) {
        super.deserialize(in);
        this.data = new ScalableDataImpl().deserialize(in);
        this.name = PacketUtils.readString(in);
        this.address = PacketUtils.readString(in);
        this.port = in.readInt();
        return this;
    }
}
