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
public final class FluentProxyDataImpl implements FluentData {

    String proxyName;
    String nodeName;
    String host;
    int port;

    @Override
    public FluentProxyDataImpl serialize(@NonNull ByteBuf out) {
        PacketUtils.writeString(out, proxyName);
        PacketUtils.writeString(out, nodeName);
        PacketUtils.writeString(out, host);
        out.writeInt(port);
        return this;
    }

    @Override
    public FluentProxyDataImpl deserialize(@NonNull ByteBuf in) {
        this.proxyName = PacketUtils.readString(in);
        this.nodeName = PacketUtils.readString(in);
        this.host = PacketUtils.readString(in);
        this.port = in.readInt();
        return this;
    }
}
