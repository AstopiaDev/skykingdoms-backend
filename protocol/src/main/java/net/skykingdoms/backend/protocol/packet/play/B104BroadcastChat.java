package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class B104BroadcastChat extends Packet<PlayProcessor> {

    String message;

    @Override
    public void process(@NotNull PlayProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
        processor.process(this, ctx);
    }

    @Override
    public boolean isProcessor(PacketProcessor processor) {
        return processor instanceof PlayProcessor;
    }

    @Override
    public void read0(@NotNull ByteBuf buf) throws Exception {
        message = PacketUtils.readString(buf);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeString(buf, message);
    }


}
