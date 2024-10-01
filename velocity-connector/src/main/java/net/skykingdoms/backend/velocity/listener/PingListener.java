package net.skykingdoms.backend.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import lombok.NonNull;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.velocity.VelocityConnector;
import net.skykingdoms.backend.velocity.connection.Connection;

public class PingListener {
    @Subscribe
    public void onPing(final @NonNull ProxyPingEvent event) {
        val response = event.getPing();
        val language = Language.DEFAULT; // todo: auto language definer
        ServerPing.Builder ping = event.getPing().asBuilder()
                .maximumPlayers(500);

        if (!Connection.isConnected()) {
            ping.onlinePlayers(0);
            ping.description(Component.text(language.getMessage(false, "motd_not_connected")));
            return;
        }

        ping.onlinePlayers(VelocityConnector.INSTANCE.getGlobalOnline());
        ping.description(Component.text(language.getMessage(false, VelocityConnector.INSTANCE.networkState().getMotd())));

        event.setPing(ping.build());
    }
}
