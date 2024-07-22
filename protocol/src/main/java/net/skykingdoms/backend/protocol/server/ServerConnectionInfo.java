package net.skykingdoms.backend.protocol.server;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.serialize.Serializable;


@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class ServerConnectionInfo implements Serializable<ServerConnectionInfo> {

    String serverName;
    String serverAddress;
    int serverPort;

    ServerScaleType scaleType;
    GameType gameType;

    @Override
    public ServerConnectionInfo serialize(final @NonNull ByteBuf out) {
        PacketUtils.writeString(out, serverName);
        PacketUtils.writeString(out, serverAddress);
        out.writeShort(serverPort);
        PacketUtils.writeEnum(out, scaleType);
        PacketUtils.writeEnum(out, gameType);
        return this;
    }

    @Override
    public ServerConnectionInfo deserialize(final @NonNull ByteBuf in) {
        serverName = PacketUtils.readString(in);
        serverAddress = PacketUtils.readString(in);
        serverPort = in.readUnsignedShort();
        scaleType = PacketUtils.readEnum(in, ServerScaleType.class);
        gameType = PacketUtils.readEnum(in, GameType.class);
        return this;
    }
}
