package net.skykingdoms.backend.restapi.endpoints;

import lombok.val;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.restapi.endpoints.impl.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

import static spark.Spark.*;

/**
 * Temporal solution, actually :nerd:
 */
public final class EndpointsManager {

    // --------------------------------------------------------------------
    public static final EndpointsManager INSTANCE = new EndpointsManager();
    // --------------------------------------------------------------------

    Collection<IEndpoint> endpoints = CollectionUtil.createConcurrentSet();

    public void initialize() {
        registerEndpoints();

        for (val processor : endpoints) {
            if (!processor.getClass().isAnnotationPresent(Endpoint.class)) {
                Logging.IMP.info("[RestAPI] No Endpoint annotation present for processor {}", processor.getClass().getName());
                continue;
            }

            if (!processor.getClass().isAnnotationPresent(ApiMethod.class)) {
                Logging.IMP.info("[RestAPI] No api method annotation present for processor {}", processor.getClass().getName());
                continue;
            }

            val endpointName = processor.getClass().getAnnotation(Endpoint.class).value();
            val type = processor.getClass().getAnnotation(ApiMethod.class).value();
            val needAuth = processor.getClass().isAnnotationPresent(AuthorizedOnly.class);

            Logging.IMP.info(endpointName);
            if (needAuth) {
                before((req, res) -> { // global lambda expression, so we verify for names
                    if (req.raw().getRequestURI().contains(endpointName)) {
                        val authHeader = req.headers("Authorization");
                        if (authHeader == null || !authHeader.startsWith("Basic ")) {
                            halt(400); // bad request
                        }

                        String credentials = authHeader.substring("Basic ".length());
                        byte[] decodedBytes = Base64.getDecoder().decode(credentials);
                        String decodedCredentials = new String(decodedBytes, StandardCharsets.UTF_8);

                        if (!MegamindApp.INSTANCE.secret().equals(decodedCredentials)) {
                            Logging.IMP.warn("[RestAPI] Unauthorized access from {} : {}", req.host(), req.userAgent());
                            res.header("WWW-Authenticate", "Basic realm=\"Restricted\"");
                            res.body("Unauthorized");
                            halt(401);
                        }
                    }
                });
            }

            Logging.IMP.info("[RestAPI] Registered {} {} endpoint {} @ {}",
                    needAuth ? "authorized" : "unauthorized",
                    type, "/api/" + endpointName, Integer.toHexString(processor.hashCode()));
            if (type == RequestType.GET) {
                get("/api/" + endpointName, processor);
            } else {
                post("/api/" + endpointName, processor);
            }
        }
    }

    public void shutdown() {
        endpoints.clear();
    }

    private void registerEndpoints() {
        endpoints.add(new PingEndpoint());
        Logging.IMP.info("Ping");
        endpoints.add(new TestflowEndpoint());
        Logging.IMP.info("Testflow");
        endpoints.add(new ReturnAddressEndpoint());
        endpoints.add(new StashEndpoint());
        endpoints.add(new PlayerDataEndpoint());
        endpoints.add(new PlayerStatusEndpoint());
        endpoints.add(new StatsEndpoint());
        endpoints.add(new PlayerListEndpoint());
        endpoints.add(new StaffListEndpoint());
        endpoints.add(new UploadEndpoint());
        endpoints.add(new RestartEndpoint());
        endpoints.add(new ReconnectEndpoint());
    }
}
