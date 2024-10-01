package net.skykingdoms.backend.protocol.type.lookup;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class LookupTarget implements Serializable<LookupTarget> {

    ScalableDataImpl data;
    int mask;

    public static LookupTarget create(final @NonNull ScalableDataImpl data,
                                      final LookupFlag @NonNull ... lookupFlags) {
        return new LookupTarget(data, LookupFlag.toMask(lookupFlags));
    }

    public static LookupTarget[] merge(final LookupTarget @NonNull [] first,
                                       final LookupTarget @NonNull [] second) {
        LookupTarget[] total = new LookupTarget[first.length + second.length];
        System.arraycopy(first, 0, total, 0, first.length);
        System.arraycopy(second, 0, total, first.length, second.length);

        Map<ScalableDataImpl, LookupTarget> merged = new HashMap<>();

        for (LookupTarget target : total) {
            LookupTarget oldTarget = merged.put(target.getData(), target);

            if (oldTarget != null) {
                target.setMask(target.mask | oldTarget.mask);
            }
        }

        return merged.values().toArray(new LookupTarget[0]);
    }

    public boolean isTarget(final @NonNull ScalableDataImpl another) {
        return data.equals(another);
    }

    public LookupFlag[] getFlags() {
        return LookupFlag.fromMask(mask);
    }

    @Override
    public LookupTarget serialize(final @NonNull ByteBuf out) {
        data.serialize(out);
        out.writeByte(mask);

        return this;
    }

    @Override
    public LookupTarget deserialize(final @NonNull ByteBuf in) {
        data = new ScalableDataImpl().deserialize(in);
        mask = in.readUnsignedByte();

        return this;
    }
}
