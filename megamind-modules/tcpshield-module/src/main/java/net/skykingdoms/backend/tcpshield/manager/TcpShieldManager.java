package net.skykingdoms.backend.tcpshield.manager;

import lombok.Getter;
import lombok.NonNull;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.util.TextFormat;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.manager.ProxyManager;
import net.tcpshield.tcpshieldapi.APIClient;
import net.tcpshield.tcpshieldapi.impl.APIClientImpl;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public final class TcpShieldManager {

    // ----------------------------------------------------------------------
    public static final TcpShieldManager INSTANCE = new TcpShieldManager();
    // ----------------------------------------------------------------------

    /**
     * List of loaded backends on remote
     */
    @Getter
    Collection<String> loadedBackends = CollectionUtil.createConcurrentSet();

    private APIClient client;
    int networkId;
    String backendSetName;
    int backendSetId;

    /**
     * Invoked once at NeoProtect module #onEnable()
     */
    public void postBootstrap(String apiKey, int networkId, String backendSetName, int backendSetId) {
        this.client = new APIClientImpl(apiKey);
        this.networkId = networkId;
        this.backendSetName = backendSetName;
        this.backendSetId = backendSetId;
        reloadBackends();
    }

    public void reloadBackends() {
        loadedBackends.clear(); // cleaning up old shit
        client.getNetworks().forEach(network -> {
            Logging.IMP.info(TextFormat.formatText("{}, {}, {}", network.getID(), network.getName(), network.getMitigationSettings().toString()));
        });
        Logging.IMP.info("[TCPShield] Loading backend servers from remote ...");
        loadedBackends.addAll(this.client.getBackendSet(networkId, backendSetId).getBackends());
        Logging.IMP.info("[TCPShield] Loaded in total {} backends", loadedBackends.size());
        Logging.IMP.info("[TCPShield] Cleaning up backends in 10 seconds ...");
        AsyncExecution.INSTANCE.runLater(this::backendCleanup, 20L, TimeUnit.SECONDS);
    }

    private void backendCleanup() {
        loadedBackends.forEach(backend -> {
            String[] splitBackend = backend.split(":");
            String ip = splitBackend[0];
            int port = Integer.parseInt(splitBackend[1]);

            Logging.IMP.info("[TCPShield] Checking for {}", backend);

            if (!ProxyManager.INSTANCE.isProxyRegistered(ip, port)) {
                Logging.IMP.info("[TCPShield] Proxy is not registered {}", backend);
                removeBackend(ip, port);
            }
        });
    }

    public void createBackend(final @NonNull String ip,
                                   final int port) {
        loadedBackends.stream().filter(backend -> backend.equals(ip + ":" + port))
                .findAny()
                .orElseGet(() ->  {
                    String backend = ip + ":" + port;
                    loadedBackends.add(backend);
                    client.patchBackendSet(networkId, backendSetId, backendSetName, loadedBackends.toArray(new String[0]));
                    Logging.IMP.info("[TCPShield] Created a backend for {}:{}", ip, port);

                    return backend;
                });
    }

    public void removeBackend(final @NonNull String ip, final int port) {
        final String backend = loadedBackends.stream()
                .filter(b -> b.equals(ip + ":" + port))
                .findAny()
                .orElse(null);

        if (backend == null) return;

        loadedBackends.remove(backend);
        client.patchBackendSet(networkId, backendSetId, backendSetName, loadedBackends.toArray(new String[0]));
        Logging.IMP.info("[TCPShield] Removed the backend {}:{}", ip, port);
    }

}
