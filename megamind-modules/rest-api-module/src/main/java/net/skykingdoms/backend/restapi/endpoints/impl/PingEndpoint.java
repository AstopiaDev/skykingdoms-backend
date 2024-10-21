package net.skykingdoms.backend.restapi.endpoints.impl;

import net.skykingdoms.backend.restapi.endpoints.ApiMethod;
import net.skykingdoms.backend.restapi.endpoints.Endpoint;
import net.skykingdoms.backend.restapi.endpoints.IEndpoint;
import net.skykingdoms.backend.restapi.endpoints.RequestType;
import spark.Request;
import spark.Response;

/**
 * Used only for API testing purposes
 * @author Vihara
 */
@ApiMethod(RequestType.GET)
@Endpoint("ping")
public final class PingEndpoint implements IEndpoint {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        return "Pong!\n";
    }
}
