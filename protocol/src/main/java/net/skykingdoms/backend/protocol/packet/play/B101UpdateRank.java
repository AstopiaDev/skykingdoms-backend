package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.util.PacketUtils;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
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
public final class B101UpdateRank extends Packet<PlayProcessor> {

    int playerId;
    Action action;

    int groupId;
    int rankId;

    int expires;

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
        playerId = PacketUtils.readVarInt(buf);
        action = PacketUtils.readEnum(buf, Action.class);

        rankId = PacketUtils.readVarInt(buf);
        groupId = PacketUtils.readVarInt(buf);

        if (action == Action.ADD_RANK) {
            expires = PacketUtils.readVarInt(buf);
        }
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeVarInt(buf, playerId);
        PacketUtils.writeEnum(buf, action);
        PacketUtils.writeVarInt(buf, rankId);
        PacketUtils.writeVarInt(buf, groupId);

        if (action == Action.ADD_RANK) {
            PacketUtils.writeVarInt(buf, expires);
        }
    }

    public enum Action {
        ADD_RANK,
        REMOVE_RANK,
        SET_RANK
    }

}
