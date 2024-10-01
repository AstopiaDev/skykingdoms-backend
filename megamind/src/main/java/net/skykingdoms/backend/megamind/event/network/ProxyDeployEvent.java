package net.skykingdoms.backend.megamind.event.network;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.backend.megamind.server.impl.ProxyServer;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public final class ProxyDeployEvent extends Event {

    ProxyServer proxyServer;

    boolean reconnect;

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }
}
