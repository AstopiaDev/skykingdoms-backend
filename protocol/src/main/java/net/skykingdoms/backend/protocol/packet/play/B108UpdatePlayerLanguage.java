package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayProcessor;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class B108UpdatePlayerLanguage extends Packet<PlayProcessor> {

    int playerId;
    int languageId;

    @Override
    public boolean isProcessor(PacketProcessor processor) {
        return processor instanceof PlayProcessor;
    }

    @Override
    protected void read0(@NotNull ByteBuf buf) throws Exception {
        playerId = buf.readInt();
        languageId = buf.readUnsignedByte();
    }

    @Override
    protected void write0(@NotNull ByteBuf buf) throws Exception {
        buf.writeInt(playerId);
        buf.writeByte(languageId);
    }

    @Override
    public void process(@NotNull PlayProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
        processor.process(this, ctx);
    }
}
