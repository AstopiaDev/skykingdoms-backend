package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class S13SendTitle extends Packet<PlayServerProcessor> {

    int receiver;
    String title;
    String subtitle;
    int fadeIn;
    int stay;
    int fadeOut;

    @Override
    public void process(@NotNull PlayServerProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
        processor.process(this, ctx);
    }

    @Override
    public boolean isProcessor(PacketProcessor processor) {
        return processor instanceof PlayServerProcessor;
    }

    @Override
    public void read0(@NotNull ByteBuf buf) throws Exception {
        receiver = buf.readInt();
        title = PacketUtils.readString(buf);
        subtitle = PacketUtils.readString(buf);
        fadeIn = PacketUtils.readVarInt(buf);
        stay = PacketUtils.readVarInt(buf);
        fadeOut = PacketUtils.readVarInt(buf);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        buf.writeInt(receiver);
        PacketUtils.writeString(buf, title);
        PacketUtils.writeString(buf, subtitle);
        PacketUtils.writeVarInt(buf, fadeIn);
        PacketUtils.writeVarInt(buf, stay);
        PacketUtils.writeVarInt(buf, fadeOut);
    }


}
