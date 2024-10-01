package net.skykingdoms.backend.protocol.packet.play;


import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

/**
 *
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class C32GetBestGameServerByMap extends Packet.Request<PlayClientProcessor, S35BestGameServerByMap> {

    String name;
    InetAddress address;
    String map;

    ScalableDataImpl data;

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
        this.name = PacketUtils.readString(buf);
        this.address = PacketUtils.readAddress(buf);
        this.map = PacketUtils.readString(buf);

        this.data = new ScalableDataImpl().deserialize(buf);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeString(buf, name);
        PacketUtils.writeAddress(buf, address);
        PacketUtils.writeString(buf, map);

        data.serialize(buf);
    }


    @Override
    public Class<S35BestGameServerByMap> getResponse() {
        return S35BestGameServerByMap.class;
    }
}
