package net.skykingdoms.api.protocol.packet.serialize;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;

@SuppressWarnings("UnusedReturnValue")
public interface Serializable<T extends Serializable<T>> {

    T serialize(final @NonNull ByteBuf out);

    T deserialize(final @NonNull ByteBuf in);

}
