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
public final class FluentNodeDataImpl implements FluentData {

    String nodeName;

    @Override
    public FluentNodeDataImpl serialize(@NonNull ByteBuf out) {
        PacketUtils.writeString(out, nodeName);
        return this;
    }

    @Override
    public FluentNodeDataImpl deserialize(@NonNull ByteBuf in) {
        this.nodeName = PacketUtils.readString(in);
        return this;
    }
}
