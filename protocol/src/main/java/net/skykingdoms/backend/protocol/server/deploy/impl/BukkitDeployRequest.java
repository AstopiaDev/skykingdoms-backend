package net.skykingdoms.backend.protocol.server.deploy.impl;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.protocol.server.deploy.AbstractDeployRequest;

import java.util.UUID;

/**
 * A request can be headed as Lobby, some special shiet or a GameServer
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
public final class BukkitDeployRequest extends AbstractDeployRequest {

    /**
     * Server scale type
     */
    ScalableDataImpl data;

    public BukkitDeployRequest(final @NonNull UUID transactionId,
                               final @NonNull ScalableDataImpl data) {
        this.transactionId = transactionId;
        this.data = data;
    }

    public BukkitDeployRequest(final @NonNull ScalableDataImpl data) {
        this(UUID.randomUUID(), data);
    }

    @Override
    public AbstractDeployRequest serialize(@NonNull ByteBuf out) {
        super.serialize(out);
        data.serialize(out);
        return this;
    }

    @Override
    public AbstractDeployRequest deserialize(@NonNull ByteBuf in) {
        super.deserialize(in);
        this.data = new ScalableDataImpl().deserialize(in);
        return this;
    }
}
