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
public final class S14SetPlayerTablist extends Packet<PlayServerProcessor> {

    int playerId;
    String header;
    String footer;

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
        playerId = buf.readInt();
        header = PacketUtils.readString(buf);
        footer = PacketUtils.readString(buf);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        buf.writeInt(playerId);
        PacketUtils.writeString(buf, header);
        PacketUtils.writeString(buf, footer);
    }
}
