package net.skykingdoms.backend.neoprotect.listener;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.event.EventHandler;
import net.skykingdoms.backend.megamind.api.plugin.Listener;
import net.skykingdoms.backend.megamind.event.network.ProxyDeployEvent;
import net.skykingdoms.backend.neoprotect.NeoProtectModule;
import net.skykingdoms.backend.neoprotect.manager.NeoProtectManager;
import net.skykingdoms.backend.protocol.logging.Logging;

public final class ProxyDeploymentListener implements Listener {

    NeoProtectModule instance;

    public ProxyDeploymentListener(final @NonNull NeoProtectModule instance) {
        MegamindApp.INSTANCE.pluginManager().registerListener(this);
        this.instance = instance;
    }

    @EventHandler
    public void onProxyDeploy(final ProxyDeployEvent event) {
        val proxy = event.getProxyServer();
        Logging.IMP.info("[NeoProtect] Deployed proxy at {}{} @ Backend ID: {}", proxy.getHost().getHostString() + ":" + proxy.getHost().getPort(),
                event.isReconnect() ? " (reconnect)" : "", NeoProtectManager.INSTANCE.requestBackendId(
                        proxy.getHost().getHostString(),
                        proxy.getHost().getPort()).getId());
    }

}
