package net.skykingdoms.backend.protocol.type.reconnect.impl;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.type.reconnect.FluentData;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class FluentServiceDataImpl implements FluentData {

    String serviceName;
    int[] eventSubscriptions;

    @Override
    public FluentServiceDataImpl serialize(@NonNull ByteBuf out) {
        PacketUtils.writeString(out, serviceName);
        return this;
    }

    @Override
    public FluentServiceDataImpl deserialize(@NonNull ByteBuf in) {
        this.serviceName = PacketUtils.readString(in);
        return this;
    }
}
