package net.skykingdoms.backend.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.CorruptedFrameException;
import lombok.NonNull;
import net.skykingdoms.api.protocol.util.PacketUtils;

import java.util.List;

public final class PacketFramer extends ByteToMessageCodec<ByteBuf> {

    @Override
    protected void encode(final @NonNull ChannelHandlerContext ctx,
                          final @NonNull ByteBuf from,
                          final @NonNull ByteBuf to) {
        int packetSize = from.readableBytes();
        int headerSize = PacketUtils.getVarIntSize(packetSize);

        to.ensureWritable(packetSize + headerSize);

        PacketUtils.writeVarInt(to, packetSize);
        to.writeBytes(from, from.readerIndex(), packetSize);
    }

    @Override
    protected void decode(final @NonNull ChannelHandlerContext ctx,
                          final @NonNull ByteBuf buf,
                          final @NonNull List<Object> out) {
        buf.markReaderIndex();

        for (int i = 0; i < 3; ++i) {
            if (!buf.isReadable()) {
                buf.resetReaderIndex();
                return;
            }

            byte b = buf.readByte();

            if (b >= 0) {
                buf.resetReaderIndex();

                int length = PacketUtils.readVarInt(buf);

                if (buf.readableBytes() < length) {
                    buf.resetReaderIndex();
                    return;
                }

                out.add(buf.slice(buf.readerIndex(), length).retain());
                buf.skipBytes(length);
                return;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }
}
