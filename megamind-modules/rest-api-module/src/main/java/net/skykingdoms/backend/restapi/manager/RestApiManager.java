package net.skykingdoms.backend.restapi.manager;

import lombok.SneakyThrows;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.restapi.endpoints.EndpointsManager;

import static spark.Spark.*;

public final class RestApiManager {

    // ----------------------------------------------------------------
    public static final RestApiManager INSTANCE = new RestApiManager();
    // ----------------------------------------------------------------

    @SneakyThrows
    public void initialize(final int port) {
        port(port);

        // adding endpoints
        EndpointsManager.INSTANCE.initialize();

        Logging.IMP.info("[RestAPI] Listening on port {}", port);
        awaitInitialization();
    }

    public void shutdown() {
        EndpointsManager.INSTANCE.shutdown();
        stop();
    }

}
