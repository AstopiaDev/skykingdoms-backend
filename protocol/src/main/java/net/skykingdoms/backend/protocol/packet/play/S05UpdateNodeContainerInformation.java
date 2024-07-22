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

import java.util.UUID;

/**
 * This packet is used to acknowledge a node that some server got
 * it's FluentData updated.
 *
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class S05UpdateNodeContainerInformation extends Packet<PlayServerProcessor> {

    /**
     * Transaction ID, that container is temporary identified with
     */
    UUID transactionId;

    /**
     * Data, realization of which is depending on remote connection type
     */
    String serverName;

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
        transactionId = UUID.fromString(PacketUtils.readString(buf));
        serverName = PacketUtils.readString(buf);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeString(buf, transactionId.toString());
        PacketUtils.writeString(buf, serverName);
    }
}