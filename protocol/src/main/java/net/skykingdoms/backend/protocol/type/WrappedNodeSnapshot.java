package net.skykingdoms.backend.protocol.type;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;


@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class WrappedNodeSnapshot implements Serializable<WrappedNodeSnapshot> {

    long creationTime;  // epoch ms of when shit started
    long startupMillis; // boot time (how long did it take to boot shit)

    int maxMemory;
    int usedMemory;
    int reservedMemory;

    @Override
    public WrappedNodeSnapshot serialize(@NonNull ByteBuf out) {
        out.writeLong(creationTime);
        out.writeLong(startupMillis);
        out.writeInt(maxMemory);
        out.writeInt(usedMemory);
        out.writeInt(reservedMemory);
        return this;
    }

    @Override
    public WrappedNodeSnapshot deserialize(@NonNull ByteBuf in) {
        this.creationTime = in.readLong();
        this.startupMillis = in.readLong();
        this.maxMemory = in.readInt();
        this.usedMemory = in.readInt();
        this.reservedMemory = in.readInt();
        return this;
    }
}
