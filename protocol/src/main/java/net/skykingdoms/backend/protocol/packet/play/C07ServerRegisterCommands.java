package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vihara 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class C07ServerRegisterCommands extends Packet<PlayClientProcessor> {
    
    Set<String> commands;

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
        commands = PacketUtils.readCollection(buf, HashSet::new, () -> PacketUtils.readString(buf));
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeCollection(buf, commands, s -> PacketUtils.writeString(buf, s));
    }


}