package net.skykingdoms.backend.neoprotect.listener;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.event.network.ProxyDeployEvent;
import net.skykingdoms.backend.megamind.event.network.ProxyRemoveEvent;
import net.skykingdoms.backend.neoprotect.NeoProtectModule;
import net.skykingdoms.backend.neoprotect.manager.NeoProtectManager;

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

    @EventHandler
    public void onProxyRemoval(final ProxyRemoveEvent event) {
        val proxy = event.getProxyServer();
/*        Logging.IMP.info("[NeoProtect] Removed proxy at {} @ Backend ID: {}", proxy.getHost().getHostString() + ":" + proxy.getHost().getPort(),
                NeoProtectManager.INSTANCE.removeBackend(
                        proxy.getHost().getHostString(),
                        proxy.getHost().getPort()).getId());*/
    }

}
