package net.skykingdoms.backend.protocol.type.reconnect;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.backend.protocol.packet.PacketUtils;

import java.util.UUID;


@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class FailedReconnectGamer implements Serializable<FailedReconnectGamer> {

    String name;
    UUID uuid;
    String reason;

    @Override
    public FailedReconnectGamer serialize(final @NonNull ByteBuf out) {
        PacketUtils.writeString(out, name);
        PacketUtils.writeString(out, uuid.toString());
        PacketUtils.writeString(out, reason);

        return this;
    }

    @SneakyThrows
    @Override
    public FailedReconnectGamer deserialize(final @NonNull ByteBuf in) {
        name = PacketUtils.readString(in);
        uuid = UUID.fromString(PacketUtils.readString(in));
        reason = PacketUtils.readString(in);

        return this;
    }
}
