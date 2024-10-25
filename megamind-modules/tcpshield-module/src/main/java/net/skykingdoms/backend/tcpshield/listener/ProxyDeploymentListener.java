package net.skykingdoms.backend.tcpshield.listener;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.event.network.ProxyDeployEvent;
import net.skykingdoms.backend.megamind.event.network.ProxyRemoveEvent;
import net.skykingdoms.backend.tcpshield.TcpShieldModule;
import net.skykingdoms.backend.tcpshield.manager.TcpShieldManager;

public final class ProxyDeploymentListener implements Listener {

    TcpShieldModule instance;

    public ProxyDeploymentListener(final @NonNull TcpShieldModule instance) {
        MegamindApp.INSTANCE.pluginManager().registerListener(this);
        this.instance = instance;
    }

    @EventHandler
    public void onProxyDeploy(final ProxyDeployEvent event) {
        val proxy = event.getProxyServer();
        TcpShieldManager.INSTANCE.createBackend(
                proxy.getHost().getHostString(),
                proxy.getHost().getPort());
        Logging.IMP.info("[TcpShield] Deployed proxy at {}{} {}", proxy.getHost().getHostString() + ":" + proxy.getHost().getPort(),
                event.isReconnect() ? " (reconnect)" : "");
    }

    @EventHandler
    public void onProxyRemoval(final ProxyRemoveEvent event) {
        val proxy = event.getProxyServer();
        val host = proxy.getHost();
        TcpShieldManager.INSTANCE.removeBackend(host.getHostString(), host.getPort());
        Logging.IMP.info("[TcpShield] Removed proxy at {}", proxy.getHost().getHostString() + ":" + proxy.getHost().getPort());
    }

}
