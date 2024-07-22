package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class C17LookupGlobalOnline extends Packet<PlayClientProcessor> {

    @Override
    public void process(@NotNull PlayClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
        processor.process(this, ctx);
    }

    @Override
    public boolean isProcessor(PacketProcessor processor) {
        return processor instanceof PlayClientProcessor;
    }

    @Override
    public void read0(@NotNull ByteBuf buf) throws Exception {
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
    }
}
