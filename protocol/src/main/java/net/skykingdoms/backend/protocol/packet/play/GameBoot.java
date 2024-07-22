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
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import net.skykingdoms.backend.protocol.type.GameBootResponse;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 */
public final class GameBoot {

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Request extends Packet.Request<PlayClientProcessor, Response> {

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayClientProcessor;
        }

        @Override
        public void read0(@NotNull ByteBuf buf) throws Exception { }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception { }

        @Override
        public void process(@NotNull PlayClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }

        @Override
        public Class<GameBoot.Response> getResponse() {
            return GameBoot.Response.class;
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Response extends Packet.Response<PlayServerProcessor> {

        GameBootResponse[] dataArray;

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
            this.dataArray = PacketUtils.readSerializableArray(buf, GameBootResponse[]::new, GameBootResponse::new);
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeSerializableArray(buf, dataArray);
        }
    }
}