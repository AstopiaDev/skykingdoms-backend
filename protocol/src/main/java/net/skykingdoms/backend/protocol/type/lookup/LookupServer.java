package net.skykingdoms.backend.protocol.type.lookup;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.api.protocol.server.GameState;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Data
@ToString(
        doNotUseGetters = true
)
@EqualsAndHashCode(
        of = {"name", "mask"},
        doNotUseGetters = true
)
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LookupServer implements Serializable<LookupServer> {

    long time;

    String name;

    ScalableDataImpl serverData;
    boolean serverOnline;

    GameState state;
    String map;

    int online = -1;
    int slots = -1;

    int[] playerIds;

    int mask;

    public LookupServer(final long time,
                        final @NonNull String name,
                        final @NonNull ScalableDataImpl data,
                        final boolean isOnline,
                        final @Nullable GameState state,
                        final @Nullable String map,
                        final int online,
                        final int slots,
                        final int[] playerIds,
                        final int mask) {
        this.time = time;
        this.serverOnline = isOnline;
        this.name = name;
        this.serverData = data;
        this.state = state;
        this.map = map;
        this.online = online;
        this.slots = slots;
        this.playerIds = playerIds;
        this.mask = mask;
    }

    public LookupServer copy() {
        return new LookupServer(time, name, serverData, serverOnline, state, map, online, slots, playerIds, mask);
    }

    public boolean hasName() {
        return name != null;
    }

    public boolean hasState() {
        return state != null;
    }

    public boolean hasMap() {
        return map != null;
    }

    public boolean hasOnline() {
        return online != -1;
    }

    public boolean hasSlots() {
        return slots != -1;
    }

    @Override
    public LookupServer serialize(final @NotNull ByteBuf out) {
        PacketUtils.writeString(out, name);
        serverData.serialize(out);
        out.writeBoolean(serverOnline);

        out.writeByte(mask);

        for (val flag : LookupFlag.fromMask(mask)) {
            flag.writeField(this, out);
        }

        return this;
    }

    @Override
    public LookupServer deserialize(final @NotNull ByteBuf in) {
        time = System.currentTimeMillis();
        name = PacketUtils.readString(in);
        serverData = new ScalableDataImpl().deserialize(in);
        serverOnline = in.readBoolean();

        mask = in.readByte();

        for (val flag : LookupFlag.fromMask(mask)) {
            flag.readField(this, in);
        }

        return this;
    }
}
