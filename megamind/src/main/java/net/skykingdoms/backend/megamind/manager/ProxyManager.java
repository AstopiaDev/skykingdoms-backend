package net.skykingdoms.backend.megamind.manager;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;
import net.skykingdoms.backend.megamind.server.impl.ProxyServer;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.play.S10RegisterCommands;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.server.deploy.impl.ProxyDeployRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manager that handles dynamic proxy deployment and load balancing between
 * bungeecord instances.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ProxyManager {

    // ------------------------------------------------------------
    public static final ProxyManager INSTANCE = new ProxyManager();
    // ------------------------------------------------------------

    ConcurrentHashMap<String, ProxyServer> proxyServers = new ConcurrentHashMap<>();
    HashSet<String> proxyServersScheduledForRemoval = new HashSet<>();


    /**
     * Invoked once. Required for initial proxy server registration
     * in case of complete network reboot.
     */
    public void postBootstrap() {
        Logging.IMP.info("[ProxyManager] Scheduled proxy bootstrap process");
        Logging.IMP.info("[ProxyManager] If any proxy connects to coordinator in next 20 seconds");
        Logging.IMP.info("[ProxyManager] The process aborts it's scheduling");
        Logging.IMP.info("");

        this.proxyCheckup();
        this.proxyRestartTask();
    }

    private void proxyRestartTask() {
        AsyncExecution.INSTANCE.runTimer(() -> {
            if (proxyServersScheduledForRemoval.isEmpty()) return;

            for (String name : proxyServersScheduledForRemoval) {
                ProxyServer proxy = getProxy(name);
                if (proxy == null) continue;

                int connectedPlayers = proxy.getPlayerIds().length;
                int maxCapacity = proxy.getSlots();
                if (connectedPlayers < maxCapacity * 0.1) {
                    removeProxyServer(name);
                } else {
                    for (int id : proxy.getPlayerIds()) {
                        Gamer gamer = Gamer.getGamer(id);
                        if (gamer != null) {
                            gamer.sendLocalizedMessage("proxy_restart_reconnect");
                        }
                    }
                }
            }

            proxyServersScheduledForRemoval.clear();
        }, 0L, 1L, TimeUnit.MINUTES);
    }

    private void proxyCheckup() {
        MegamindApp.INSTANCE.scheduler().schedule(() -> {
            if (!proxyServers.isEmpty()) return; // some proxy registered

            Logging.IMP.info("[ProxyManager] No proxy server came online! Bootstrapping cluster of proxy servers ...");

            DeploymentManager.INSTANCE.sendDeploymentRequest(new ProxyDeployRequest(), ServerDeploy.DeploymentTarget.PROXY, false, null, 0, $ -> {});
        }, 20, TimeUnit.SECONDS);
    }

    public ProxyServer getProxy(final @NonNull String name) {
        return proxyServers.getOrDefault(name, null);
    }

    public boolean isProxyRegistered(final @NonNull String name) {
        return proxyServers.containsKey(name);
    }

    public boolean isProxyRegistered(final @NonNull String ip, final int port) {
        for (ProxyServer proxyServer : proxyServers.values()) {
            val host = proxyServer.getHost();

            if (host.getHostString().equals(ip) && host.getPort() == port) {
                return true;
            }
        }

        return false;
    }

    public void sendToProxies(final @NonNull Packet<?> packet) {
        proxyServers.values().forEach(proxyServer -> proxyServer.getChannel().sendPacket(packet));
    }

    public void scheduleProxyForRemoval(final @NonNull String name) {
        ProxyServer proxy = getProxy(name);

        if (proxy == null) {
            return;
        }

        proxy.setJoinable(false);
        proxyServersScheduledForRemoval.add(name);
    }

    /**
     * Just registers a proxy server instance
     */
    public ProxyServer registerProxyServer(final @NonNull ProxyServer proxyServer) {
        if (isProxyRegistered(proxyServer.getName())) {
            throw new IllegalArgumentException("Proxy with this name is already registered!");
        }

        Logging.IMP.info("[Proxy] Registered proxy {}", proxyServer.getName());

        proxyServers.put(proxyServer.getName(), proxyServer);

        return proxyServer;
    }

    /**
     * Unregistering a proxy by its name
     */
    public void removeProxyServer(final @NonNull String name) {
        val proxy = proxyServers.remove(name);
        if (proxy == null) {
            return;
        }

        Logging.IMP.info("[Proxy] Proxy {} went down at {} with {} players online",
                proxy.getName(),
                proxy.getNode().getName(),
                proxy.getPlayerIds().length);

        // Invoking player quit event for every player connected to this proxy
        for (val playerId : proxy.getPlayerIds()) {
            Gamer.getGamer(playerId).quit();
        }

        // If last proxy got unregistered we should
        // deploy new instance, so players would
        // have an ability to connect back 0_0
        if (proxyServers.isEmpty()) {
            Logging.IMP.info("It looks like the latest proxy server got unregistered, and now no active proxies are running!");
            Logging.IMP.info("If in next 10 seconds no proxy server comes online we deploy a new proxy instance...");

            MegamindApp.INSTANCE.scheduler().schedule(() -> {
                if (!proxyServers.isEmpty()) return;

                Logging.IMP.info("No proxy server came online! Bootstrapping new proxy instance ...");

                //DeploymentManager.INSTANCE.sendDeploymentRequest(new ProxyDeployRequest(), ServerDeploy.DeploymentTarget.PROXY, false, null, 0, $ -> {});
            }, 10, TimeUnit.SECONDS);
        }
    }

    /**
     * Getting a proxy instance with smaller amount of players connected
     */
    public ProxyServer getOptimalProxy() {
        return proxyServers.values().stream()
                .filter(AbstractPlayerHolder::canJoin)
                .min(Comparator.comparingInt(a -> a.getPlayerIds().length))
                .orElse(null);
    }

}