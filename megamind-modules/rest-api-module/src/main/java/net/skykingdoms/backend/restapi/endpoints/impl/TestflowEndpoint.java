package net.skykingdoms.backend.restapi.endpoints.impl;

import lombok.SneakyThrows;
import lombok.val;
import net.skykingdoms.backend.megamind.manager.TestFlowManager;
import net.skykingdoms.backend.restapi.endpoints.*;
import org.jetbrains.annotations.NotNull;
import spark.Request;
import spark.Response;

import java.io.FileInputStream;

@ApiMethod(RequestType.GET)
@Endpoint("testflow")
@AuthorizedOnly
public final class TestflowEndpoint implements IEndpoint {

    @SneakyThrows
    @Override
    public Object handle(final @NotNull Request request, final @NotNull Response response) {
        val token = request.queryParams("token");
        if (token == null) {
            response.status(401); // eblan-code
            return "dolbaeb nice token men)))";
        }

        val testFlowData = TestFlowManager.INSTANCE.getTestFlow(request.queryParams("name"));
        if (testFlowData == null) {
            response.status(500); // internal server error
            return "TestFlow with this name doesn't exist!\n";
        }

        val testFlowZip = TestFlowManager.INSTANCE.createZipFile(testFlowData);
        val fis = new FileInputStream(testFlowZip);

        val buffer = new byte[64 * 1024];  // 64KB buffer
        int bytesRead;

        val outputStream = response.raw().getOutputStream();

        while ((bytesRead = fis.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        testFlowZip.delete();

        outputStream.close();

        return null;
    }
}
