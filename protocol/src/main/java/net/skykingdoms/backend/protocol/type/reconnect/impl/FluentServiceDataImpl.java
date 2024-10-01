package net.skykingdoms.backend.protocol.type.reconnect.impl;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.type.reconnect.FluentData;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class FluentServiceDataImpl implements FluentData {

    @Setter String serviceName;
    @Setter Class<? extends Event>[] eventSubscriptions;

    @Override
    public FluentServiceDataImpl serialize(@NonNull ByteBuf out) {
        PacketUtils.writeString(out, serviceName);
        PacketUtils.writeArray(out, eventSubscriptions, eventSubscription -> PacketUtils.writeString(out, eventSubscription.getName()));
        return this;
    }

    @Override
    public FluentServiceDataImpl deserialize(@NonNull ByteBuf in) {
        this.serviceName = PacketUtils.readString(in);
        this.eventSubscriptions = PacketUtils.readArray(in, Class[]::new, () -> {
            try {
                return (Class<? extends Event>) Class.forName(PacketUtils.readString(in));
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
        return this;
    }

    @SafeVarargs
    public final void setEventSubscriptions(Class<? extends Event>... events) {
        this.eventSubscriptions = events;
    }
}
