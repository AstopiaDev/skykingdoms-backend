package net.skykingdoms.backend.restapi.endpoints;

import lombok.NonNull;
import spark.Response;
import spark.Route;

public interface IEndpoint extends Route {

    default String terminate(final @NonNull Response response,
                            final int code,
                            final @NonNull String message) {
        response.status(code);
        return message;
    }

}
