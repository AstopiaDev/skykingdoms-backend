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
import org.jetbrains.annotations.NotNull;


public final class ServerConnect {

    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Request extends Packet.Request<PlayClientProcessor, Response> {

        private int playerId;
        private String target;

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
            playerId = PacketUtils.readVarInt(buf);
            target = PacketUtils.readString(buf);
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeVarInt(buf, playerId);
            PacketUtils.writeString(buf, target);
        }

        @Override
        public Class<ServerConnect.Response> getResponse() {
            return ServerConnect.Response.class;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Response extends Packet.Response<PlayServerProcessor> {

        private boolean cancelled;
        private String newServer;

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
            cancelled = buf.readBoolean();
            newServer = PacketUtils.readString(buf);
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            buf.writeBoolean(cancelled);
            PacketUtils.writeString(buf, newServer);
        }
    }
}
