package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import net.skykingdoms.backend.protocol.type.reconnect.FluentData;
import org.jetbrains.annotations.NotNull;


@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class S04UpdateRemoteData extends Packet<PlayServerProcessor> {

    /**
     * Type of remote connected client
     */
    RemoteConnectedType remoteConnectedType;

    /**
     * Data, realization of which is depending on remote connection type
     */
    FluentData serverDataUpdate;

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
        remoteConnectedType = PacketUtils.readEnum(buf, RemoteConnectedType.class);
        this.serverDataUpdate = FluentData.deserialize(remoteConnectedType, buf);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeEnum(buf, remoteConnectedType);
        serverDataUpdate.serialize(buf);
    }
}