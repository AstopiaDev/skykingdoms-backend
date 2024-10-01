package net.skykingdoms.api.protocol.server.impl;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.IScalable;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.api.protocol.util.PacketUtils;

import java.util.Objects;


@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@ToString
public final class ScalableDataImpl implements IScalable, Serializable<ScalableDataImpl> {

    ServerScaleType serverScale;
    GameType gameType;
    GameMode gameMode;

    @Override
    public ScalableDataImpl serialize(@NonNull ByteBuf out) {
        PacketUtils.writeEnum(out, serverScale);
        PacketUtils.writeEnum(out, gameType);
        if (serverScale.hasGameMode()) {
            PacketUtils.writeEnum(out, gameMode);
        }
        return this;
    }

    @Override
    public ScalableDataImpl deserialize(@NonNull ByteBuf in) {
        this.serverScale = PacketUtils.readEnum(in, ServerScaleType.class);
        this.gameType = PacketUtils.readEnum(in, GameType.class);
        if (serverScale.hasGameMode()) {
            this.gameMode = PacketUtils.readEnum(in, GameMode.class);
        }
        return this;
    }

    public boolean isGameArena() {
        return serverScale == ServerScaleType.MEGA || serverScale == ServerScaleType.MINI;
    }

    public boolean isLobby() {
        return serverScale == ServerScaleType.LOBBY;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ScalableDataImpl
                && ((ScalableDataImpl) obj).getServerScale() == serverScale
                && ((ScalableDataImpl) obj).getGameType() == gameType
                && ((ScalableDataImpl) obj).getGameMode() == gameMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverScale, gameType, gameMode);
    }
}
