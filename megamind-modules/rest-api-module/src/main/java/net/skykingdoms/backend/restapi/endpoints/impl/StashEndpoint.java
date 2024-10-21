package net.skykingdoms.backend.restapi.endpoints.impl;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.backend.megamind.manager.StashManager;
import net.skykingdoms.backend.restapi.endpoints.*;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@ApiMethod(RequestType.GET)
@Endpoint("retrieve")
@AuthorizedOnly
public final class StashEndpoint implements IEndpoint{

    @Override
    public Object handle(Request request, Response response) {
        val token = request.queryParams("token");
        if (token == null || !token.equalsIgnoreCase("cock")) {
            response.status(401);
            return "dolbaeb nice token men)))";
        }


        File stashedZip;
        if (request.queryParams("lobby") != null) {
            stashedZip = StashManager.INSTANCE.getLobbyAssets(GameType.valueOf(request.queryParams("name")));
        } else {
            stashedZip = StashManager.INSTANCE.createZipFile(request.queryParams("name"));
        }

        try {
            writeServletOutputStream(stashedZip, response);
        } catch (Exception e) {
            Logging.IMP.stacktrace(e, "Fuiceld.");
        }

        return null;
    }

    private void writeServletOutputStream(final @NonNull File stashedZip,
                                          final Response response) throws IOException {
        val fis = new FileInputStream(stashedZip);

        val buffer = new byte[64 * 1024];  // 64KB buffer
        int bytesRead;

        val outputStream = response.raw().getOutputStream();

        while ((bytesRead = fis.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        stashedZip.delete();

        outputStream.close();
    }

}
