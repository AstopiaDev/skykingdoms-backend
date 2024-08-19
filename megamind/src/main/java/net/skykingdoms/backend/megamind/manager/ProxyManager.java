package net.skykingdoms.backend.megamind.manager;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.backend.base.util.collection.CollectionUtil;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.instance.impl.ProxyInstance;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.play.S10RegisterCommands;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.server.deploy.impl.ProxyDeployRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manager that handles dynamic proxy deployment and load balancing between
 * bungeecord instances.
 *
 *
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ProxyManager {

    // ------------------------------------------------------------
    public static final ProxyManager INSTANCE = new ProxyManager();
    // ------------------------------------------------------------

    Collection<ProxyInstance> proxyInstances = CollectionUtil.createConcurrentSet();

    /**
     * Invoked once. Required for initial proxy instance registration
     * in case of complete network reboot.
     */
    public void postBootstrap() {
        Logging.IMP.info("Scheduled proxy bootstrap process");
        Logging.IMP.info("If any proxy connects to coordinator in next 20 seconds");
        Logging.IMP.info("The process aborts it's scheduling");
        Logging.IMP.info("");

        MegamindApp.INSTANCE.scheduler().schedule(() -> {
            if (!proxyInstances.isEmpty()) return; // some proxy registered

            Logging.IMP.info("No proxy instance came online! Bootstrapping cluster of proxy servers ...");

            DeploymentManager.INSTANCE.sendDeploymentRequest(new ProxyDeployRequest(), ServerDeploy.DeploymentTarget.PROXY, false, null, 0, $ -> {});
        }, 20, TimeUnit.SECONDS);
    }

    public ProxyInstance getProxy(final @NonNull String name) {
        return proxyInstances.stream()
                .filter(proxyServer -> proxyServer.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isProxyRegistered(final @NonNull String name) {
        return proxyInstances.stream()
                .anyMatch(proxyServer -> proxyServer.getName().equalsIgnoreCase(name));
    }

    public void sendToProxies(final @NonNull Packet<?> packet) {
        proxyInstances.forEach(proxyServer -> proxyServer.getChannel().sendPacket(packet));
    }

    /**
     * Just registers a proxy instance instance
     */
    public ProxyInstance registerProxyServer(final @NonNull ProxyInstance proxyInstance) {
        if (isProxyRegistered(proxyInstance.getName())) {
            throw new IllegalArgumentException("Proxy with this name is already registered!");
        }

        Logging.IMP.info("[Proxy] Registered proxy {}", proxyInstance.getName());

        proxyInstances.add(proxyInstance);

        List<S10RegisterCommands.CommandContainer> containers = new ArrayList<>();
        for (val entry : InstanceManager.INSTANCE.getCommandMapper().getCommandsEntry()) {
            containers.add(new S10RegisterCommands.CommandContainer(
                    entry.getValue(), S10RegisterCommands.CommandSource.SERVER, entry.getKey()
            ));
        }

        containers.add(S10RegisterCommands.CommandContainer.coreSingleton(MegamindApp.INSTANCE.getCommands())[0]);

        proxyInstance.getChannel().sendPacket(new S10RegisterCommands(containers.toArray(S10RegisterCommands.CommandContainer[]::new)));

        return proxyInstance;
    }

    /**
     * Unregistering a proxy by its name
     */
    public void removeProxyServer(final @NonNull String name) {
        val proxy = getProxy(name);
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

        proxyInstances.remove(proxy);

        // If last proxy got unregistered we should
        // deploy new instance, so players would
        // have an ability to connect back 0_0
        if (proxyInstances.isEmpty()) {
            Logging.IMP.info("It looks like the latest proxy instance got unregistered, and now no active proxies are running!");
            Logging.IMP.info("If in next 10 seconds no proxy instance comes online we deploy a new proxy instance...");

            MegamindApp.INSTANCE.scheduler().schedule(() -> {
                if (!proxyInstances.isEmpty()) return;

                Logging.IMP.info("No proxy instance came online! Bootstrapping new proxy instance ...");

                //DeploymentManager.INSTANCE.sendDeploymentRequest(new ProxyDeployRequest(), ServerDeploy.DeploymentTarget.PROXY, false, null, 0, $ -> {});
            }, 10, TimeUnit.SECONDS);
        }
    }

    /**
     * Getting a proxy instance with smaller amount of players connected
     */
    public ProxyInstance getOptimalProxy() {
        return proxyInstances.stream()
                .min(Comparator.comparingInt(a -> a.getPlayerIds().length))
                .orElse(null);
    }

}