package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import net.skykingdoms.backend.protocol.type.lookup.LookupTarget;
import org.jetbrains.annotations.NotNull;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public final class C16RegisterLookups extends Packet<PlayClientProcessor> {

    LookupTarget[] targets;

    public C16RegisterLookups(LookupTarget... targets) {
        this.targets = targets;
    }

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
        targets = PacketUtils.readSerializableArray(buf, LookupTarget[]::new, LookupTarget::new);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeSerializableArray(buf, targets);
    }
}
