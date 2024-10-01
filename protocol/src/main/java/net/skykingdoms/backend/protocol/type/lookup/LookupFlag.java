package net.skykingdoms.backend.protocol.type.lookup;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.skykingdoms.api.protocol.server.GameState;
import net.skykingdoms.api.protocol.util.ArrayIterator;
import net.skykingdoms.backend.protocol.packet.PacketUtils;

import java.util.Iterator;

@Getter
public enum LookupFlag {

    /**
     * Отслежка изменения онлайна / слотов
     */
    ON_ONLINE_UPDATE { // << 0 => 00_00_00_01 => 1

        @Override
        public void writeField(LookupServer server, ByteBuf buf) {
            PacketUtils.writeVarInt(buf, server.getOnline());
            PacketUtils.writeVarInt(buf, server.getSlots());
        }

        @Override
        public void readField(LookupServer server, ByteBuf buf) {
            server.setOnline(PacketUtils.readVarInt(buf));
            server.setSlots(PacketUtils.readVarInt(buf));
        }

        @Override
        public void mergeField(LookupServer first, LookupServer second) {
            second.setOnline(first.getOnline());
            second.setSlots(first.getSlots());
        }
    },
    ON_PLAYERS_UPDATE { // << 1 => 00_00_00_10 => 2

        @Override
        public void writeField(LookupServer server, ByteBuf buf) {
            PacketUtils.writeIntArray(buf, server.getPlayerIds());
        }

        @Override
        public void readField(LookupServer server, ByteBuf buf) {
            server.setPlayerIds(PacketUtils.readIntArray(buf));
        }

        @Override
        public void mergeField(LookupServer first, LookupServer second) {
            second.setPlayerIds(first.getPlayerIds());
        }
    },

    /**
     * Отслежка изменения карты
     *
     * <p>
     * Пока что нужно только для того, чтобы вернуть карту
     * <br>когда отправляется пакет Lookup
     * </p>
     */
    ON_MAP_UPDATE { // << 3 => 00_00_01_00 => 4

        @Override
        public void writeField(LookupServer server, ByteBuf buf) {
            PacketUtils.writeString(buf, server.getMap());
        }

        @Override
        public void readField(LookupServer server, ByteBuf buf) {
            server.setMap(PacketUtils.readString(buf));
        }

        @Override
        public void mergeField(LookupServer first, LookupServer second) {
            second.setMap(first.getMap());
        }
    },

    /**
     * Отслежка изменения статуса арены
     */
    ON_STATE_UPDATE { // << 4 => 00_00_10_00 => 8

        @Override
        public void writeField(LookupServer server, ByteBuf buf) {
            PacketUtils.writeEnum(buf, server.getState());
        }

        @Override
        public void readField(LookupServer server, ByteBuf buf) {
            server.setState(PacketUtils.readEnum(buf, GameState.class));
        }

        @Override
        public void mergeField(LookupServer first, LookupServer second) {
            second.setState(first.getState());
        }
    },

    ON_RESULT_UPDATE; // << 5 => 00_01_00_00 => 16

    public static final LookupFlag[] VALUES = LookupFlag.values();
    public static final LookupFlag[] EMPTY = new LookupFlag[0];

    public static final int MASK = 0b11111;

    private final int mask = 1 << ordinal();

    public static int toMask(Iterator<LookupFlag> flagIterator) {
        int mask = 0;

        while (flagIterator.hasNext()) {
            mask |= flagIterator.next().getMask();
        }

        return mask;
    }

    public static int toMask(LookupFlag... flags) {
        return toMask(new ArrayIterator<>(flags));
    }

    public static boolean hasFlag(int mask, LookupFlag flag) {
        return (mask & flag.getMask()) != 0;
    }

    public static LookupFlag[] fromMask(int mask) {
        if (mask == 0) {
            return EMPTY;
        }

        int count = Integer.bitCount(mask);
        int flag = 0;

        LookupFlag[] flags = new LookupFlag[count];

        while (mask != 0) {
            if ((mask & 1) == 1) {
                flags[--count] = VALUES[flag];
                if (count == 0) break;
            }

            flag++;
            mask >>= 1;
        }

        return flags;
    }

    public void writeField(LookupServer server, ByteBuf buf) {
        // override me
    }

    public void readField(LookupServer server, ByteBuf buf) {
        // override me
    }

    public void mergeField(LookupServer first, LookupServer second) {
        // override me
    }

}