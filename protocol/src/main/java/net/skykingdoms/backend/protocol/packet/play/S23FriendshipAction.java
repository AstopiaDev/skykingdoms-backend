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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class S23FriendshipAction extends Packet<PlayServerProcessor> {

    // Player ID
    int friendshipId;

    // Id of
    int firstPlayerId;
    int secondPlayerId;

    Action action;

    // null if action = REMOVE_FRIEND
    LocalDateTime localDateTime;

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
        friendshipId = buf.readInt();
        firstPlayerId = buf.readInt();
        secondPlayerId = buf.readInt();
        action = PacketUtils.readEnum(buf, Action.class);
        if (action == Action.ADD_FRIEND) {
            localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(buf.readLong()), ZoneId.systemDefault());
        }
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        buf.writeInt(friendshipId);
        buf.writeInt(firstPlayerId);
        buf.writeInt(secondPlayerId);
        PacketUtils.writeEnum(buf, action);
        if (action == Action.ADD_FRIEND) {
            buf.writeLong(ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
    }

    public enum Action {
        ADD_FRIEND, REMOVE_FRIEND
    }

}
