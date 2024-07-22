package net.skykingdoms.backend.megamind.event.network;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.plugin.Event;
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

}
