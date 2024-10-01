package net.skykingdoms.backend.megamind.manager;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;

import java.security.SecureRandom;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public final class CoreSecurityManager {

    // --------------------------------------------------------------------------
    public static final CoreSecurityManager INSTANCE = new CoreSecurityManager();
    // --------------------------------------------------------------------------

    /**
     * A token we use to inject in bungee protocol
     */
    volatile String bungeeSecurityToken;

    /**
     * Token for internal communications (TODO)
     */
    volatile String internalCommsToken;

    // characters to use to build a token
    private static final String TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Randomly generates a new token
     *
     * @param length    the length of the token
     * @return          a new token
     */
    private String generateToken(final int length) {
        Preconditions.checkArgument(length > 0);

        val sb = new StringBuilder();
        val random = new SecureRandom();
        for (var i = 0; i < length; i++) {
            sb.append(TOKEN_CHARS.charAt(random.nextInt(TOKEN_CHARS.length())));
        }

        return sb.toString();
    }

    /**
     * Invoked once on server startup
     */
    public void postBootstrap() {
        bungeeSecurityToken = generateToken(128);
        internalCommsToken = generateToken(128);

        Logging.IMP.info("[Security] Initialized security manager!");
        Logging.IMP.info("[Security] Bungee token: {}", bungeeSecurityToken);
        Logging.IMP.info("[Security] Internal token: {}", internalCommsToken);
    }

    /**
     * Silly, todo: refactor
     */
    public boolean isInitialized() {
        return bungeeSecurityToken != null && internalCommsToken != null;
    }
}
