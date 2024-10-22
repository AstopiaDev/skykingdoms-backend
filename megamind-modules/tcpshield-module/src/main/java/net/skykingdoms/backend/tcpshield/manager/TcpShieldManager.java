package net.skykingdoms.backend.tcpshield.manager;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.tcpshield.TcpShieldModule;
import net.skykingdoms.backend.tcpshield.dao.Backend;
import net.skykingdoms.backend.tcpshield.util.JsonBuilder;
import net.skykingdoms.backend.tcpshield.util.TcpShieldHttpUtil;
import net.skykingdoms.backend.tcpshield.util.TcpShieldRequestType;
import net.tcpshield.tcpshieldapi.APIClient;
import net.tcpshield.tcpshieldapi.impl.APIClientImpl;

import java.util.Collection;

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
    int backendSetId;

    /**
     * Invoked once at NeoProtect module #onEnable()
     */
    public void postBootstrap(String apiKey, int networkId, int backendSetId) {
        this.client = new APIClientImpl(apiKey);
        this.networkId = networkId;
        this.backendSetId = backendSetId;
        reloadBackends();
    }

    public void reloadBackends() {
        loadedBackends.clear(); // cleaning up old shit

        Logging.IMP.info("[TCPShield] Loading backend servers from remote ...");
        loadedBackends.addAll(this.client.getBackendSet(networkId, backendSetId).getBackends());
        Logging.IMP.info("[TCPShield] Loaded in total {} backends", loadedBackends.size());
        Logging.IMP.info(this.client.getBackendSet(networkId, backendSetId).toString());
    }

/*    public Backend requestBackendId(final @NonNull String ip,
                                   final int port) {
        return loadedBackends.stream().filter(backend -> backend.equals(ip) && backend.getPort() == port)
                .findAny()
                .orElseGet(() ->  {
                    val requestBody = RequestBody.create(MediaType.parse("application/json"), new JsonBuilder()
                            .appendField("host", ip)
                            .appendField("port", port)
                            .build().toString());

                    Logging.IMP.info(TcpShieldModule.INSTANCE.getBackendGroupId());
                    val response = TcpShieldHttpUtil.request(TcpShieldRequestType.POST_BACKEND, requestBody, TcpShieldModule.INSTANCE.getServerId(), TcpShieldModule.INSTANCE.getBackendGroupId());

                    Logging.IMP.info("[NeoProtect] Requested backend ID for {}:{} -> {}", ip, port, response.getResponse().toString());

                    //val response = NeoProtectHttpUtil.request(NeoRequestType.POST_BACKEND, requestBody, NeoProtectModule.INSTANCE.getServerId(), );
                    *//*return new NetworkPlayerLoad.Response()*//*
                    // todo: implement dynamic routing
                    return new Backend(
                            "a0aa7cef-d26d-4853-ac82-c32862330fbf",
                            ip,
                            port,
                            false
                    );
                });
    }*/

/*    public void removeBackend(final @NonNull String id) {
        final Backend backend = loadedBackends.stream().filter(b -> b.getId().equals(id)).findAny().orElse(null);
        if (backend == null) return;

        loadedBackends.remove(backend);
        RequestBody.create(MediaType.parse("application/json"), new JsonBuilder()
                .appendField("serverId", TcpShieldModule.INSTANCE.getServerId())
                .appendField("groupId", id)
                .appendField("backendId", backend.getIp())
                .build().toString());
    }*/

}
