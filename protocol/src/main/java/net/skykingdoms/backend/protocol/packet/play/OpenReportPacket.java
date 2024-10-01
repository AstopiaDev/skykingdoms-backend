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
import net.skykingdoms.api.protocol.type.ReportType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 */
public class OpenReportPacket {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Request extends Packet.Request<PlayClientProcessor, Response> {

        int senderId;
        int targetId;
        ReportType reportType;

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayClientProcessor;
        }

        @Override
        public void read0(@NotNull ByteBuf buf) throws Exception {
            senderId = buf.readInt();
            targetId = buf.readInt();
            reportType = PacketUtils.readEnum(buf, ReportType.class);
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            buf.writeInt(senderId);
            buf.writeInt(targetId);
            PacketUtils.writeEnum(buf, reportType);
        }

        @Override
        public void process(@NotNull PlayClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }

        @Override
        public Class<OpenReportPacket.Response> getResponse() {
            return OpenReportPacket.Response.class;
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Response extends Packet.Response<PlayServerProcessor> {

        boolean openedState;
        String errorMessage;

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
            openedState = buf.readBoolean();
            if (!openedState) {
                errorMessage = PacketUtils.readString(buf);
            }
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            buf.writeBoolean(openedState);
            if (!openedState) {
                PacketUtils.writeString(buf, errorMessage);
            }
        }
    }
}
