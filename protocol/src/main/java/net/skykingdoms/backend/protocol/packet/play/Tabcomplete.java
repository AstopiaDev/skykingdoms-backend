package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class Tabcomplete {

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static final class Request extends Packet.Request<PlayClientProcessor, Response> {

        int playerId;
        String line;

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
            playerId = buf.readInt();
            line = PacketUtils.readString(buf);
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            buf.writeInt(playerId);
            PacketUtils.writeString(buf, line);
        }

        @Override
        public Class<Tabcomplete.Response> getResponse() {
            return Tabcomplete.Response.class;
        }
    }

    /**
     *
     */
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static final class Response extends Packet.Response<PlayServerProcessor> {

        List<String> suggestions;

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
            suggestions = PacketUtils.readCollection(buf, ArrayList::new, () -> PacketUtils.readString(buf));
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeCollection(buf, suggestions, s -> PacketUtils.writeString(buf, s));
        }
    }

}
