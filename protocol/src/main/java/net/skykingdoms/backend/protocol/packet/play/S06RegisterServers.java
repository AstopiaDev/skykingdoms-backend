package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import net.skykingdoms.backend.protocol.server.ServerConnectionInfo;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class S06RegisterServers extends Packet<PlayServerProcessor> {

    private ServerConnectionInfo[] servers;

    public S06RegisterServers(ServerConnectionInfo... servers) {
        this.servers = servers;
    }

    public S06RegisterServers(String name,
                              String address,
                              int port,
                              ServerScaleType scaleType,
                              GameType gameType) {
        this(new ServerConnectionInfo(name, address, port, scaleType, gameType));
    }

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
        servers = PacketUtils.readSerializableArray(buf, ServerConnectionInfo[]::new, ServerConnectionInfo::new);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeSerializableArray(buf, servers);
    }

}
