package net.skykingdoms.backend.protocol.server.deploy.impl;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.skykingdoms.backend.protocol.server.deploy.AbstractDeployRequest;

import java.util.UUID;

/**
 * No special fields declared in this request, as the only thing we must know -
 * we deploy a PROXY server :)
 *
 */
public final class ProxyDeployRequest extends AbstractDeployRequest {

    public ProxyDeployRequest(final @NonNull UUID transactionId) {
        super(transactionId);
    }

    public ProxyDeployRequest() {
        super(UUID.randomUUID());
    }

    @Override
    public ProxyDeployRequest serialize(@NonNull ByteBuf out) {
        super.serialize(out);
        return this;
    }

    @Override
    public ProxyDeployRequest deserialize(@NonNull ByteBuf in) {
        super.deserialize(in);
        return this;
    }
}
