package net.skykingdoms.backend.restapi.manager;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.restapi.RestPlugin;
import net.skykingdoms.backend.restapi.endpoints.EndpointsManager;

import static spark.Spark.awaitInitialization;
import static spark.Spark.port;

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

}
