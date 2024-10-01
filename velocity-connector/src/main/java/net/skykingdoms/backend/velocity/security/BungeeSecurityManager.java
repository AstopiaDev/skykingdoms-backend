package net.skykingdoms.backend.velocity.security;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.data.security.AbstractHandshakeSecurityManager;
import net.skykingdoms.backend.velocity.VelocityConnector;
import net.skykingdoms.backend.velocity.security.impl.SpoofedLoginResult;

/*
 * Singleton manager, corresponding to handshake validation
 * , [github: lucko]
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
public final class BungeeSecurityManager extends AbstractHandshakeSecurityManager {

    // ------------------------------------------------------------------
    public static final BungeeSecurityManager INSTANCE = new BungeeSecurityManager();
    // ------------------------------------------------------------------


/*
     * Invoked once to register required listeners
     */

    public void postBootstrap() {
        VelocityConnector.INSTANCE.server().getEventManager().register(VelocityConnector.INSTANCE.boot(), new BungeeSecurityListener());
    }


/*
     * After coordinator reboot we need to update connection data
     * so players will be able to move between servers
     */

    public void updatePlayersConnections() {
        for (val player : VelocityConnector.INSTANCE.server().getAllPlayers()) {
            SpoofedLoginResult.inject(player, BungeeSecurityManager.INSTANCE.getSecurityToken());
            Logging.IMP.info("[Security] Injected data into {}'s connection", player.getUsername());
        }
    }

/*
     * Updates security token
     * @param token new token
     */

    public void updateToken(final @NonNull String token) {
        super.updateToken(token);

        // todo: "blur" part of it to keep logs safe
        Logging.IMP.info("[Security] Updated token to " + token);
    }

}
