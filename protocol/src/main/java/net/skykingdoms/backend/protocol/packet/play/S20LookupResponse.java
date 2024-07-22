package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import net.skykingdoms.backend.protocol.type.lookup.LookupResponse;
import org.jetbrains.annotations.NotNull;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public final class S20LookupResponse extends Packet<PlayServerProcessor> {

    LookupResponse response;

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
        response = new LookupResponse().deserialize(buf);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        response.serialize(buf);
    }
}
