package net.skykingdoms.backend.bukkit.security;

import lombok.NonNull;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.data.security.AbstractHandshakeSecurityManager;
import net.skykingdoms.backend.bukkit.BukkitConnector;
import net.skykingdoms.backend.bukkit.security.impl.SecurityHandshakeListener;

/**
 * @author Vihara
 */
public final class BukkitSecurityManager extends AbstractHandshakeSecurityManager {

    // ------------------------------------------------------------------------------
    public static final BukkitSecurityManager INSTANCE = new BukkitSecurityManager();
    // ------------------------------------------------------------------------------

    /**
     * Invoked once at plugin #onEnable()
     */
    public void postBootstrap() {
        BukkitConnector.INSTANCE.plugin().getServer().getPluginManager().registerEvents(new SecurityHandshakeListener(), BukkitConnector.INSTANCE.plugin());
    }

    /**
     * Updates security token
     * @param token new token
     */
    public void updateToken(final @NonNull String token) {
        super.updateToken(token);

        // todo: "blur" part of it to keep logs safe
        Logging.IMP.info("[Security] Updated token to " + token);
    }

}
