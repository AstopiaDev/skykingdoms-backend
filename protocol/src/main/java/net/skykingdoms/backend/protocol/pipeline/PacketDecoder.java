package net.skykingdoms.backend.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.backend.protocol.packet.PacketDirection;
import net.skykingdoms.backend.protocol.packet.PacketProtocol;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PacketDecoder extends ByteToMessageDecoder {
    final PacketDirection direction;
    PacketProtocol state = PacketProtocol.HANDSHAKE;

    public void upgradeConnection(final @NonNull PacketProtocol newState) {
        state = newState;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, @NotNull ByteBuf buf, List<Object> out) {
        if (buf.readableBytes() == 0) {
            return;
        }

        int id = PacketUtils.readVarInt(buf);

        try {
            val packet = direction.getMapper(state).newPacket(id);

            if (packet == null) {
                System.out.println("Couldn't find packet with id: " + id);
                return;
            }

            packet.read(buf);
            out.add(packet);
        } catch (Exception ex) {
            System.out.println("Exception caught while decoding packet: " + id + ": " + ex.getMessage());
        }
    }
}
