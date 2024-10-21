package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.type.GameMode;
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
public final class B110UserUpdatePermission extends Packet<PlayProcessor> {

    int playerId;
    Action action;

    String permission;
    boolean value;

    GameMode gameMode;
    boolean hasGameMode;

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
        permission = PacketUtils.readString(buf);
        hasGameMode = buf.readBoolean();

        if (hasGameMode) {
            gameMode = PacketUtils.readEnum(buf, GameMode.class);
        }

        if (action == Action.ADD_PERMISSION || action == Action.SET_PERMISSION) {
            value = buf.readBoolean();
            expires = PacketUtils.readVarInt(buf);
        }
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeVarInt(buf, playerId);
        PacketUtils.writeEnum(buf, action);
        PacketUtils.writeString(buf, permission);
        buf.writeBoolean(hasGameMode);

        if (hasGameMode) {
            PacketUtils.writeEnum(buf, gameMode);
        }

        if (action == Action.ADD_PERMISSION || action == Action.SET_PERMISSION) {
            buf.writeBoolean(value);
            PacketUtils.writeVarInt(buf, expires);
        }
    }

    public enum Action {
        ADD_PERMISSION,
        SET_PERMISSION,
        REMOVE_PERMISSION,
    }

}
