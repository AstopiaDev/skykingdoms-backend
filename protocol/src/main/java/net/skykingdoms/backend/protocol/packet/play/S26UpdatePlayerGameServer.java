package net.skykingdoms.backend.protocol.packet.play;


import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class S26UpdatePlayerGameServer extends Packet<PlayServerProcessor> {

    int gamerId;
    int arenaId;

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
        this.gamerId = buf.readInt();
        this.arenaId = buf.readInt();
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        buf.writeInt(gamerId);
        buf.writeInt(arenaId);
    }
}