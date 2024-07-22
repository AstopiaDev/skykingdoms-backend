package net.skykingdoms.backend.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketDirection;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketProtocol;
import net.skykingdoms.backend.protocol.util.PacketUtils;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PacketEncoder extends MessageToByteEncoder<Packet<PacketProcessor>> {
    final PacketDirection direction;
    PacketProtocol state = PacketProtocol.HANDSHAKE;

    public void upgradeConnection(final @NonNull PacketProtocol newState) {
        state = newState;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, @NotNull Packet<PacketProcessor> packet, ByteBuf out) throws Exception {
        val packetId = direction.getMapper(state).getPacketId(packet.getClass());
        if (packetId == -1) {
            System.out.println("Tried to encode an unregistered packet! Packet: " + packet);
            return;
        }

        PacketUtils.writeVarInt(out, packetId);
        packet.write(out);
    }
}
