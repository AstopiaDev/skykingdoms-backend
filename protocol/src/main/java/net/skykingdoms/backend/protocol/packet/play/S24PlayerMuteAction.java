package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * We send this packet on bungee to cancel chat events for player
 * @author Vihara
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class S24PlayerMuteAction extends Packet<PlayServerProcessor> {

    Action action;
    int muteId;

    int violatorId;
    String reason;
    LocalDateTime expiration;

    // we don't pass execution date and executor ID because we don't care
    // about those fields on bungee

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
        action = PacketUtils.readEnum(buf, Action.class);
        muteId = buf.readInt();
        if (action == Action.REMOVE_MUTE) {
            return;
        }

        violatorId = buf.readInt();
        reason = PacketUtils.readString(buf);
        expiration = LocalDateTime.ofInstant(Instant.ofEpochMilli(buf.readLong()), ZoneId.systemDefault());
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeEnum(buf, action);
        buf.writeInt(muteId);
        if (action == Action.REMOVE_MUTE) {
            return;
        }

        buf.writeInt(violatorId);
        PacketUtils.writeString(buf, reason);
        buf.writeLong(ZonedDateTime.of(expiration, ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public enum Action {
        ADD_MUTE,
        REMOVE_MUTE
    }

}
