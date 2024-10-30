package net.skykingdoms.backend.velocity.boot;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.skykingdoms.backend.velocity.VelocityConnector;

import java.util.logging.Logger;

@Plugin(
        id = "velocity-connector",
        name = "Velocity Connector",
        version = "1.0",
        authors = {
                "SkyKingdoms"
        }
)
public class VelocityConnectorBoot {
    @Inject
    public VelocityConnectorBoot(ProxyServer server, Logger logger) {
        VelocityConnector.INSTANCE.construct(this, server);
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        if (!VelocityConnector.INSTANCE.initialized()) {
            VelocityConnector.INSTANCE.init();
        }
    }
}
