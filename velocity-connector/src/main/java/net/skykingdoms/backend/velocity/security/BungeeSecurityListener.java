package net.skykingdoms.backend.velocity.security;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.velocity.security.impl.SpoofedLoginResult;

public final class BungeeSecurityListener {
    @Subscribe
    public void onLogin(final LoginEvent e) {
        if (!BungeeSecurityManager.INSTANCE.isInitialized()) {
            Logging.IMP.warn("[Security] SecurityManager was not initialized yet!");
            // todo: make this red
            e.setResult(ResultedEvent.ComponentResult.denied(Component.text("Internal server error\n[SEC-1]")));
            return;
        }

        // inject our spoofed LoginResult instance into the initial handler
        val con = e.getPlayer();
        SpoofedLoginResult.inject(con, BungeeSecurityManager.INSTANCE.getSecurityToken());
        Logging.IMP.info("[Security] Injected data into {}'s connection", con.getUsername());
    }
}
