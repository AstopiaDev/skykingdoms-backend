package net.skykingdoms.backend.protocol.server.deploy;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.PacketUtils;

import java.util.UUID;


@Getter
@FieldDefaults(level = AccessLevel.PROTECTED)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public abstract class AbstractDeployRequest implements IDeployRequest {

    UUID transactionId;

    @Override
    public AbstractDeployRequest serialize(@NonNull ByteBuf out) {
        PacketUtils.writeString(out, transactionId.toString());
        return this;
    }

    @Override
    public AbstractDeployRequest deserialize(@NonNull ByteBuf in) {
        this.transactionId = UUID.fromString(PacketUtils.readString(in));
        return this;
    }
}
