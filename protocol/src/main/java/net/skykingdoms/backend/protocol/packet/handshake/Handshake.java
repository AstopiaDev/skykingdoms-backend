package net.skykingdoms.backend.protocol.packet.handshake;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.handshake.processor.HandshakeClientProcessor;
import net.skykingdoms.backend.protocol.packet.handshake.processor.HandshakeServerProcessor;
import net.skykingdoms.backend.protocol.type.reconnect.FluentData;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentNodeDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentProxyDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentServiceDataImpl;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.UUID;

public final class Handshake {

    private Handshake() {
        throw new UnsupportedOperationException();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static final class Request extends Packet.Request<HandshakeClientProcessor, Response> {
        // Type of client
        RemoteConnectedType type;

        // Secret authentication key
        String secret;

        // Deployment transaction ID (in case of node it's random)
        UUID transactionId;

        EnumSet<RequestFlag> flags;

        FluentData fluentData;

        int testFlowId;

        public boolean isTestFlow() {
            return flags.contains(RequestFlag.IS_TESTFLOW);
        }

        public boolean isReconnect() {
            return flags.contains(RequestFlag.IS_RECONNECT);
        }

        public boolean hasData() {
            return flags.contains(RequestFlag.HAS_FLUENT_DATA);
        }

        @Override
        public void process(@NotNull HandshakeClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof HandshakeClientProcessor;
        }

        @Override
        public void read0(@NotNull ByteBuf buf) throws Exception {
            type = PacketUtils.readEnum(buf, RemoteConnectedType.class);
            secret = PacketUtils.readString(buf);
            transactionId = UUID.fromString(PacketUtils.readString(buf));
            flags = EnumSet.noneOf(RequestFlag.class);
            byte flagByte = buf.readByte();
            if ((flagByte & 0b00001) != 0) flags.add(RequestFlag.HAS_FLUENT_DATA);
            if ((flagByte & 0b00010) != 0) flags.add(RequestFlag.IS_RECONNECT);
            if ((flagByte & 0b00100) != 0) flags.add(RequestFlag.IS_TESTFLOW);
            if (hasData()) {
                switch (type) {
                    case PROXY: {
                        fluentData = new FluentProxyDataImpl().deserialize(buf);
                        break;
                    }
                    case SERVICE: {
                        fluentData = new FluentServiceDataImpl().deserialize(buf);
                        break;
                    }
                    case NODE: {
                        fluentData = new FluentNodeDataImpl().deserialize(buf);
                        break;
                    }
                    case SERVER: {
                        fluentData = new FluentBukkitDataImpl().deserialize(buf);
                        break;
                    }
                }
            }
            if (isTestFlow()) {
                testFlowId = buf.readInt();
            }
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeEnum(buf, type);
            PacketUtils.writeString(buf, secret);
            PacketUtils.writeString(buf, transactionId.toString());
            byte flagByte = 0;
            if (flags.contains(RequestFlag.HAS_FLUENT_DATA)) flagByte |= 0b00001;
            if (flags.contains(RequestFlag.IS_RECONNECT)) flagByte |= 0b00010;
            if (flags.contains(RequestFlag.IS_TESTFLOW)) flagByte |= 0b00100;
            buf.writeByte(flagByte);
            if (hasData()) {
                fluentData.serialize(buf);
            }
            if (isTestFlow()) {
                buf.writeInt(testFlowId);
            }
        }

        @Override
        public Class<Handshake.Response> getResponse() {
            return Handshake.Response.class;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static final class Response extends Packet.Response<HandshakeServerProcessor> {

        Result result;
        String message;

        @Override
        public void process(@NotNull HandshakeServerProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof HandshakeServerProcessor;
        }

        @Override
        public void read0(@NotNull ByteBuf buf) throws Exception {
            result = PacketUtils.readEnum(buf, Result.class);

            if (result == Result.FAILED) {
                message = PacketUtils.readString(buf);
            }
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeEnum(buf, result);

            if (result == Result.FAILED) {
                PacketUtils.writeString(buf, message);
            }
        }
    }

    public enum Result {
        FAILED, SUCCESS
    }

    public enum RequestFlag {
        HAS_FLUENT_DATA,
        IS_RECONNECT,
        IS_TESTFLOW
    }
}
