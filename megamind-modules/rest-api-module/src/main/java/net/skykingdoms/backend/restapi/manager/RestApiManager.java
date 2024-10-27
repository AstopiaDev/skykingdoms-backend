package net.skykingdoms.backend.restapi.manager;

import lombok.SneakyThrows;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.restapi.endpoints.EndpointsManager;
import spark.Filter;
import spark.Request;
import spark.Response;

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
        enableCORS("*", "*", "*");
        awaitInitialization();
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {
        before(new Filter() {
            @Override
            public void handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", origin);
                response.header("Access-Control-Request-Method", methods);
                response.header("Access-Control-Allow-Headers", headers);
            }
        });
    }

    public void shutdown() {
        EndpointsManager.INSTANCE.shutdown();
        stop();
    }

}
