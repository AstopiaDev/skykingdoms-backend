package net.skykingdoms.backend.velocity.security.impl;

import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.GameProfile;
import lombok.NonNull;
import lombok.val;

/**
 * Extension of {@link GameProfile} which returns a modified Property array when
 * {@link } is called by the ServerConnector implementation.
 * <p>
 * To achieve this, the stack trace is analyzed. This is kinda crappy, but is the only way
 * to modify the properties without leaking the token to other clients via the tablist.
 */
public abstract class SpoofedLoginResult {
    public static void inject(final @NonNull InboundConnection handler,
                              final @NonNull String token) {
        val properties = ((Player) handler).getGameProfileProperties();
        properties.add(new GameProfile.Property("security-token", token, ""));
        ((Player) handler).setGameProfileProperties(properties);
    }
}