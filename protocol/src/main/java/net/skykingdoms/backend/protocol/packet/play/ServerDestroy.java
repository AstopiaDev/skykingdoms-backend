package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import org.jetbrains.annotations.NotNull;


public final class ServerDestroy {

    public ServerDestroy() {
        throw new UnsupportedOperationException();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static final class Request extends Packet.Request<PlayServerProcessor, Response> {

        /**
         * Specified container instance name
         */
        String serverName;

        /**
         * Should we handle shit gracefully,
         * or we basically need to stop and remove container by cocker
         */
        boolean graceful;

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayServerProcessor;
        }

        @Override
        protected void read0(@NotNull ByteBuf buf) throws Exception {
            serverName = PacketUtils.readString(buf);
             graceful = buf.readBoolean();
        }

        @Override
        protected void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeString(buf, serverName);
             buf.writeBoolean(graceful);
        }

        @Override
        public Class<ServerDestroy.Response> getResponse() {
            return ServerDestroy.Response.class;
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

        /**
         * Indicates if node found specified container instance and destroyed it
         */
        boolean destroyed;

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayClientProcessor;
        }

        @Override
        protected void read0(@NotNull ByteBuf buf) throws Exception {
            destroyed = buf.readBoolean();
        }

        @Override
        protected void write0(@NotNull ByteBuf buf) throws Exception {
            buf.writeBoolean(destroyed);
        }

        @Override
        public void process(@NotNull PlayClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }
    }

    /**
     * Used to indicate, if our deployment is accepted on node.
     *
     *
     */
    public enum DeploymentResult {
        ACCEPTED, DENIED
    }

}
