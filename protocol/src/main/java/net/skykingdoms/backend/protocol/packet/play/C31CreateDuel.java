package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import net.skykingdoms.backend.protocol.type.duel.FluentDuelRequestData;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class C31CreateDuel extends Packet<PlayClientProcessor> {
    int senderId;
    int targetId;
    GameMode mode;
    FluentDuelRequestData data;

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
        this.senderId = buf.readInt();
        this.targetId = buf.readInt();
        this.mode = PacketUtils.readEnum(buf, GameMode.class);
        this.data = FluentDuelRequestData.deserialize(mode, buf);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        buf.writeInt(senderId);
        buf.writeInt(targetId);
        PacketUtils.writeEnum(buf, mode);
        data.serialize(buf);
    }
}