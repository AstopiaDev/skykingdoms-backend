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

import java.net.InetAddress;
import java.util.UUID;


public final class ProxyPlayerLogin {

    private ProxyPlayerLogin() {
        throw new UnsupportedOperationException();
    }

    public static final byte VERIFY = 0b001;
    public static final byte FINISH = 0b010;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Request extends Packet.Request<PlayClientProcessor, Response> {

        UUID uuid;
        String name;

        InetAddress address;
        String domain;

        int protocol;
        boolean premium;
        byte mask;

        public boolean contains(int i) {
            return (mask & i) != 0;
        }

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayClientProcessor;
        }

        @Override
        public void read0(@NotNull ByteBuf buf) throws Exception {
            uuid = UUID.fromString(PacketUtils.readString(buf));
            name = PacketUtils.readString(buf);

            address = PacketUtils.readAddress(buf);
            premium = buf.readBoolean();
            mask = buf.readByte();

            if (contains(FINISH)) {
                domain = PacketUtils.readString(buf);
                protocol = PacketUtils.readVarInt(buf);
            }
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeString(buf, uuid.toString());
            PacketUtils.writeString(buf, name);

            PacketUtils.writeAddress(buf, address);
            buf.writeBoolean(premium);
            buf.writeByte(mask);

            if (contains(FINISH)) {
                PacketUtils.writeString(buf, domain);
                PacketUtils.writeVarInt(buf, protocol);
            }
        }

        @Override
        public void process(@NotNull PlayClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }

        @Override
        public Class<ProxyPlayerLogin.Response> getResponse() {
            return ProxyPlayerLogin.Response.class;
        }

    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Response extends Packet.Response<PlayServerProcessor> {

        boolean cancelled;
        boolean sessionLogin;
        String message;

        public Response(boolean cancelled, boolean sessionLogin, String message) {
            this.cancelled = cancelled;
            this.sessionLogin = sessionLogin;;

            if (cancelled && message == null) {
                message = "";
            }

            this.message = message;
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
            cancelled = buf.readBoolean();

            if (cancelled) {
                message = PacketUtils.readString(buf);
            } else {
                sessionLogin = buf.readBoolean();
            }
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            buf.writeBoolean(cancelled);

            if (cancelled) {
                PacketUtils.writeString(buf, message);
            } else {
                buf.writeBoolean(sessionLogin);
            }
        }
    }
}
