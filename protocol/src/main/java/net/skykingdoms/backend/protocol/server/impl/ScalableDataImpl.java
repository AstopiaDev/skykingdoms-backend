package net.skykingdoms.backend.protocol.server.impl;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.serialize.Serializable;
import net.skykingdoms.backend.protocol.server.GameMode;
import net.skykingdoms.backend.protocol.server.GameType;
import net.skykingdoms.backend.protocol.server.IScalable;
import net.skykingdoms.backend.protocol.server.ServerScaleType;

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
