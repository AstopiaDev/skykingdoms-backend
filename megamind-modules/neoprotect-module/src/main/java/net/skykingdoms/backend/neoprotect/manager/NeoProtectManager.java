package net.skykingdoms.backend.neoprotect.manager;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.util.collection.CollectionUtil;
import net.skykingdoms.backend.neoprotect.NeoProtectModule;
import net.skykingdoms.backend.neoprotect.dao.Backend;
import net.skykingdoms.backend.neoprotect.util.JsonBuilder;
import net.skykingdoms.backend.neoprotect.util.NeoProtectHttpUtil;
import net.skykingdoms.backend.neoprotect.util.NeoRequestType;
import net.skykingdoms.backend.protocol.logging.Logging;

import java.util.Collection;

public final class NeoProtectManager {

    // ----------------------------------------------------------------------
    public static final NeoProtectManager INSTANCE = new NeoProtectManager();
    // ----------------------------------------------------------------------

    /**
     * List of loaded backends on remote
     */
    @Getter
    Collection<Backend> loadedBackends = CollectionUtil.createConcurrentSet();

    /**
     * Invoked once at NeoProtect module #onEnable()
     */
    public void postBootstrap() {
        reloadBackends();
    }

    public void reloadBackends() {
        loadedBackends.clear(); // cleaning up old shit

        Logging.IMP.info("[NeoProtect] Loading backend servers from remote ...");
        loadedBackends.addAll(NeoProtectHttpUtil.retrieveBackends());
        Logging.IMP.info("[NeoProtect] Loaded in total {} backends", loadedBackends.size());

        NeoProtectManager.INSTANCE.requestBackendId(
                "5.9.150.40",
                40056
        );
    }

    public Backend requestBackendId(final @NonNull String ip,
                                   final int port) {
        return loadedBackends.stream().filter(backend -> backend.getIp().equals(ip) && backend.getPort() == port)
                .findAny()
                .orElseGet(() -> {
                    val requestBody = RequestBody.create(MediaType.parse("application/json"), new JsonBuilder()
                            .appendField("host", ip)
                            .appendField("port", port)
                            .build().toString());
                    Logging.IMP.info(NeoProtectModule.INSTANCE.getBackendGroupId());
                    val response = NeoProtectHttpUtil.request(NeoRequestType.POST_BACKEND, requestBody, NeoProtectModule.INSTANCE.getServerId(), NeoProtectModule.INSTANCE.getBackendGroupId());

                    Logging.IMP.info("[NeoProtect] Requested backend ID for {}:{} -> {}", ip, port, response.getResponse().toString());

                    //val response = NeoProtectHttpUtil.request(NeoRequestType.POST_BACKEND, requestBody, NeoProtectModule.INSTANCE.getServerId(), );
                    /*return new NetworkPlayerLoad.Response()*/
                    // todo: implement dynamic routing
                    return new Backend(
                            "a0aa7cef-d26d-4853-ac82-c32862330fbf",
                            ip,
                            port,
                            false
                    );
                });
    }

    public void removeBackend(final @NonNull String id) {
        final Backend backend = loadedBackends.stream().filter(b -> b.getId().equals(id)).findAny().orElse(null);
        if (backend == null) return;

        loadedBackends.remove(backend);
        RequestBody.create(MediaType.parse("application/json"), new JsonBuilder()
                .appendField("serverId", NeoProtectModule.INSTANCE.getServerId())
                .appendField("groupId", id)
                .appendField("backendId", backend.getIp())
                .build().toString());
    }

}
