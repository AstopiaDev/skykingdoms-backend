package net.skykingdoms.backend.protocol.type;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This dataclass is used in {@link Handshake.Request}
 * to determine which server is connecting to coordinator.
 *
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor // required for deserialize installation
@Data
public final class WrappedServerTransmittedInfo implements Serializable<WrappedServerTransmittedInfo> {

    /**
     * Full name of server
     */
    @NotNull String serverName;

    /**
     * Server scale type
     */
    @NotNull ServerScaleType scaleType;

    /**
     * Name of node which server is deployed on
     */
    @NotNull String deployedNodeName;

    /**
     * Type of this game server (in case of undefined server it
     */
    @Nullable GameType gameType;

    // Written if mode is MINI or MEGA (in case of lobby etc. it's not used)
    @Nullable GameMode gameMode;

    public String getReadableInformation() {
        return  "Name: " + serverName + "\n" +
                "Deployed on node: " + deployedNodeName + "\n" +
                "Scale type: " + scaleType + "\n" +
                (scaleType == ServerScaleType.MEGA
                        || scaleType == ServerScaleType.MINI
                        ?   "Game type: " + gameType + "\n" +
                            (gameType != GameType.MAIN ? "Mode: " + gameMode : "")
                        : "");
    }

    @Override
    public WrappedServerTransmittedInfo serialize(@NonNull ByteBuf out) {
        PacketUtils.writeString(out, serverName);
        PacketUtils.writeString(out, deployedNodeName);
        PacketUtils.writeEnum(out, scaleType);
        if (scaleType == ServerScaleType.MEGA
                || scaleType == ServerScaleType.MINI || scaleType == ServerScaleType.PERSISTENT) {
            PacketUtils.writeEnum(out, gameType);
            if (gameType != GameType.MAIN) { // main game type don't have any game modes
                PacketUtils.writeEnum(out, gameMode);
            }
        }
        return this;
    }

    @Override
    public WrappedServerTransmittedInfo deserialize(@NonNull ByteBuf in) {
        this.serverName = PacketUtils.readString(in);
        this.deployedNodeName = PacketUtils.readString(in);
        this.scaleType = PacketUtils.readEnum(in, ServerScaleType.class);

        // If server is defined as LOBBY / MINI / LOBBY it must have a game mode linked
        if (this.scaleType == ServerScaleType.MINI
                || this.scaleType == ServerScaleType.MEGA || this.scaleType == ServerScaleType.PERSISTENT) {
            this.gameType = PacketUtils.readEnum(in, GameType.class);
            if (this.gameType != GameType.MAIN) {
                this.gameMode = PacketUtils.readEnum(in, GameMode.class);
            }
        }
        return this;
    }
}
