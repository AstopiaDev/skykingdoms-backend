package net.skykingdoms.backend.protocol.type.reconnect;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class ReconnectingGamer implements Serializable<ReconnectingGamer> {

    String name;
    UUID uuid;
    InetAddress address;

    String domain;
    int protocol;
    boolean loggedIn;

    String server;

    @Override
    public ReconnectingGamer serialize(@NotNull ByteBuf out) {
        PacketUtils.writeString(out, name);
        PacketUtils.writeString(out, uuid.toString());
        PacketUtils.writeAddress(out, address);
        PacketUtils.writeString(out, server);
        PacketUtils.writeString(out, domain);
        PacketUtils.writeVarInt(out, protocol);
        out.writeBoolean(loggedIn);

        return this;
    }

    @SneakyThrows
    @Override
    public ReconnectingGamer deserialize(@NotNull ByteBuf in) {
        name = PacketUtils.readString(in);
        uuid = UUID.fromString(PacketUtils.readString(in));
        address = PacketUtils.readAddress(in);
        server = PacketUtils.readString(in);
        domain = PacketUtils.readString(in);
        protocol = PacketUtils.readVarInt(in);
        loggedIn = in.readBoolean();

        return this;
    }
}
