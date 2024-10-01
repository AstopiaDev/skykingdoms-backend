package net.skykingdoms.backend.protocol.type.reconnect;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentNodeDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentProxyDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentServiceDataImpl;

/**
 * A basic class that represents basic fluent information on server
 * 
 */
@SuppressWarnings("unchecked")
public interface FluentData extends Serializable<FluentData> {

    /**
     * Actually, this shouldn't be realized like this, but who cars, it's 1AM
     *
     * @param type  type of remote client
     * @param buf   byte buffer
     * @return      deserialized FluentData object
     * @param <T>   generic type of fluent data superclass
     */
    static <T extends FluentData> T deserialize(final @NonNull RemoteConnectedType type,
                                                final @NonNull ByteBuf buf) {
        switch (type) {
            case SERVICE: {
                return (T) new FluentServiceDataImpl().deserialize(buf);
            }
            case NODE: {
                return (T) new FluentNodeDataImpl().deserialize(buf);
            }
            case PROXY: {
                return (T) new FluentProxyDataImpl().deserialize(buf);
            }
            case SERVER: {
                return (T) new FluentBukkitDataImpl().deserialize(buf);
            }
        }

        return null;
    }

    static <T extends FluentData> T deserialize(final int mask,
                                                final @NonNull ByteBuf buf) {
        return deserialize(RemoteConnectedType.byMask(mask), buf);
    }

}
