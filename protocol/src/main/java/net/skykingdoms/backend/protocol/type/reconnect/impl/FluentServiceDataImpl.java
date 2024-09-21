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

    @Setter String serviceName;
    @Setter String[] eventSubscriptions;

    @Override
    public FluentServiceDataImpl serialize(@NonNull ByteBuf out) {
        PacketUtils.writeString(out, serviceName);
        PacketUtils.writeArray(out, eventSubscriptions, eventSubscription -> PacketUtils.writeString(out, eventSubscription));
        return this;
    }

    @Override
    public FluentServiceDataImpl deserialize(@NonNull ByteBuf in) {
        this.serviceName = PacketUtils.readString(in);
        this.eventSubscriptions = PacketUtils.readArray(in, String[]::new, () -> PacketUtils.readString(in));
        return this;
    }
}
