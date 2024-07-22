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
public final class B109UpdateSetting extends Packet<PlayProcessor> {

    int playerId;
    String settingName;

    // we send it as string, but it may be int. decoded at the endpoint
    String serializedValue;

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
        playerId = buf.readInt();
        settingName = PacketUtils.readString(buf);
        serializedValue = PacketUtils.readString(buf);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        buf.writeInt(playerId);
        PacketUtils.writeString(buf, settingName);
        PacketUtils.writeString(buf, serializedValue);
    }

}
