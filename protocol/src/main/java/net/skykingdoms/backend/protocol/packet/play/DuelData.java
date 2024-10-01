package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import net.skykingdoms.backend.protocol.type.duel.FluentDuelRequestData;
import org.jetbrains.annotations.NotNull;

public final class DuelData {

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Request extends Packet.Request<PlayClientProcessor, Response> {

        int gamerId;

        public Request(final int gamerId) {
            this.gamerId = gamerId;
        }

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayClientProcessor;
        }

        @Override
        public void read0(@NotNull ByteBuf buf) throws Exception {
            gamerId = buf.readInt();
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            buf.writeInt(gamerId);
        }

        @Override
        public void process(@NotNull PlayClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }

        @Override
        public Class<DuelData.Response> getResponse() {
            return DuelData.Response.class;
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Response extends Packet.Response<PlayServerProcessor> {

        int senderId;
        int targetId;
        GameMode mode;
        FluentDuelRequestData data;

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
            this.senderId = buf.readInt();
            this.targetId = buf.readInt();
            this.mode = PacketUtils.readEnum(buf, GameMode.class, true);
            this.data = FluentDuelRequestData.deserialize(mode, buf);
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            buf.writeInt(senderId);
            buf.writeInt(targetId);
            Logging.IMP.info("Writing mode: " + mode);
            PacketUtils.writeEnum(buf, mode, true);
            data.serialize(buf);
        }
    }
}
