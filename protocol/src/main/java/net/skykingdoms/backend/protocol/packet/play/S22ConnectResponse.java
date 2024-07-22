package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import net.skykingdoms.backend.protocol.type.connect.ConnectStatus;
import org.jetbrains.annotations.NotNull;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class S22ConnectResponse extends Packet.Response<PlayServerProcessor> {

    ConnectStatus status;

    public boolean isSuccess() {
        return status == ConnectStatus.SUCCESS;
    }

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
        status = PacketUtils.readEnum(buf, ConnectStatus.class, true);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeEnum(buf, status, true);
    }

}