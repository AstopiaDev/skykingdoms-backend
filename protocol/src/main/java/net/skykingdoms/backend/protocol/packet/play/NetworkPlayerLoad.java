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

import java.util.ArrayList;
import java.util.List;


public final class NetworkPlayerLoad {

    public static final int IS_ONLINE = 1;

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @Data
    @RequiredArgsConstructor
    public static class PlayerHolder {
        boolean online;
        int playerId;
        int protocol;
        boolean loggedIn;

        final String server;

        public PlayerHolder(int playerId) {
            this(false, playerId, 0, false, null);
        }

        public PlayerHolder(int playerId, String server) {
            this(true, playerId, 0, false, server);
        }

        public PlayerHolder(int playerId, String server, int protocol) {
            this(true, playerId, protocol, false, server);
        }

        public PlayerHolder(int playerId, String server, int protocol, boolean loggedIn) {
            this(true, playerId, protocol, loggedIn, server);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response extends Packet.Response<PlayServerProcessor> {

        private List<PlayerHolder> players;

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayServerProcessor;
        }

        @Override
        public void read0(@NotNull ByteBuf buf) throws Exception {
            players = PacketUtils.readCollection(buf, ArrayList::new, () -> {
                int mask = buf.readUnsignedByte();
                int playerId = PacketUtils.readVarInt(buf);
                int protocol = PacketUtils.readVarInt(buf);

                boolean online = false;
                boolean loggedIn = false;
                String server = null;

                if ((mask & IS_ONLINE) == IS_ONLINE) {
                    online = true;
                    loggedIn = buf.readBoolean();
                    server = PacketUtils.readString(buf);
                }

                return new PlayerHolder(online, playerId, protocol, loggedIn, server);
            });
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeCollection(buf, players, player -> {
                buf.writeByte((player.online ? IS_ONLINE : 0));
                PacketUtils.writeVarInt(buf, player.playerId);
                PacketUtils.writeVarInt(buf, player.protocol);

                if (player.online) {
                    buf.writeBoolean(player.loggedIn);
                    PacketUtils.writeString(buf, player.server);
                }
            });
        }

        @Override
        public void process(@NotNull PlayServerProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request extends Packet.Request<PlayClientProcessor, Response> {

        int[] playerIds;
        int mask;

        public Request(int playerId, int mask) {
            this(new int[] {playerId}, mask);
        }

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayClientProcessor;
        }

        @Override
        public void read0(@NotNull ByteBuf buf) throws Exception {
            playerIds = PacketUtils.readVarIntArray(buf);
            mask = buf.readUnsignedByte();
        }

        @Override
        public void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeVarIntArray(buf, playerIds);
            buf.writeByte(mask);
        }

        @Override
        public void process(@NotNull PlayClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }

        @Override
        public Class<NetworkPlayerLoad.Response> getResponse() {
            return NetworkPlayerLoad.Response.class;
        }
    }


}
