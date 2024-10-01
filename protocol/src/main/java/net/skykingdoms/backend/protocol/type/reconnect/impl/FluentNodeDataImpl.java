package net.skykingdoms.backend.protocol.type.reconnect.impl;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.type.reconnect.FluentData;


@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class FluentNodeDataImpl implements FluentData {

    String nodeName;
    ServerScaleType[] scaleTypes;
    int allocatedRamMB;
    int maxRamMB;

    @Override
    public FluentNodeDataImpl serialize(@NonNull ByteBuf out) {
        PacketUtils.writeString(out, nodeName);
        PacketUtils.writeArray(out, scaleTypes, scaleType -> PacketUtils.writeString(out, scaleType.name()));
        out.writeInt(allocatedRamMB);
        out.writeInt(maxRamMB);
        return this;
    }

    @Override
    public FluentNodeDataImpl deserialize(@NonNull ByteBuf in) {
        this.nodeName = PacketUtils.readString(in);
        this.scaleTypes = PacketUtils.readArray(in, ServerScaleType[]::new, () -> ServerScaleType.valueOf(PacketUtils.readString(in)));
        this.allocatedRamMB = in.readInt();
        this.maxRamMB = in.readInt();
        return this;
    }
}
