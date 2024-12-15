package net.skykingdoms.backend.restapi.endpoints.impl;

import lombok.val;
import net.skykingdoms.backend.restapi.endpoints.*;
import spark.Request;
import spark.Response;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Sends list of registered nodes/developers
 */
@ApiMethod(RequestType.GET)
@Endpoint("punishment/:id")
@AuthorizedOnly
public final class PunishmentDetailsEndpoint implements IEndpoint {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        val punishmentId = request.params(":id");

        return punishmentId;
    }
}