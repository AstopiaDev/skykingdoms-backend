package net.skykingdoms.backend.protocol.server.deploy;

import lombok.NonNull;
import net.skykingdoms.backend.protocol.packet.serialize.Serializable;
import net.skykingdoms.backend.protocol.server.ServerScaleType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public interface IDeployRequest extends Serializable<IDeployRequest> {

    /**
     * Unique ID of deployment request (transaction)
     */
    @NotNull UUID getTransactionId();
}
