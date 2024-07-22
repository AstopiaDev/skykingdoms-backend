package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import net.skykingdoms.backend.protocol.type.WrappedNodeSnapshot;
import org.jetbrains.annotations.NotNull;


public final class NodeSnapshot {

    public NodeSnapshot() {
        throw new UnsupportedOperationException();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @Data
    public static final class Request extends Packet.Request<PlayServerProcessor, Response> {

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayServerProcessor;
        }

        @Override
        protected void read0(@NotNull ByteBuf buf) throws Exception {

        }

        @Override
        protected void write0(@NotNull ByteBuf buf) throws Exception {

        }

        @Override
        public Class<NodeSnapshot.Response> getResponse() {
            return NodeSnapshot.Response.class;
        }

        @Override
        public void process(@NotNull PlayServerProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
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
    public static final class Response extends Packet.Response<PlayClientProcessor> {

        WrappedNodeSnapshot wrappedNodeSnapshot;

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayClientProcessor;
        }

        @Override
        protected void read0(@NotNull ByteBuf buf) throws Exception {
            wrappedNodeSnapshot = new WrappedNodeSnapshot().deserialize(buf);
        }

        @Override
        protected void write0(@NotNull ByteBuf buf) throws Exception {
            wrappedNodeSnapshot.serialize(buf);
        }


        @Override
        public void process(@NotNull PlayClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }
    }
}
