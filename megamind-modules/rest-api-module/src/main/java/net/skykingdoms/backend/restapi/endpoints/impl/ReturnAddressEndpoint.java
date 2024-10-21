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
@Endpoint("retaddr")
@AuthorizedOnly
public final class ReturnAddressEndpoint implements IEndpoint {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        val br = new BufferedReader(new FileReader("security.cock"));

        val sb = new StringBuilder();
        String buf;
        while ((buf = br.readLine()) != null) {
            sb.append(buf).append("\n");
        }

        response.body(sb.toString());
        return sb.toString();
    }
}