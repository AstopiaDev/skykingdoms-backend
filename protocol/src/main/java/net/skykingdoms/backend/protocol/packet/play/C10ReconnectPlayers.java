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
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import net.skykingdoms.backend.protocol.type.reconnect.ReconnectingGamer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class C10ReconnectPlayers extends Packet.Request<PlayClientProcessor, S12FailedReconnectPlayers> {

    private Set<ReconnectingGamer> gamers;

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
        gamers = PacketUtils.readSerializableCollection(buf, HashSet::new, ReconnectingGamer::new);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeSerializableCollection(buf, gamers);
    }

    @Override
    public Class<S12FailedReconnectPlayers> getResponse() {
        return S12FailedReconnectPlayers.class;
    }
}
