package net.skykingdoms.backend.restapi.endpoints.impl;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.restapi.endpoints.*;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@ApiMethod(RequestType.POST)
@Endpoint("upload")
@AuthorizedOnly
public final class UploadEndpoint implements IEndpoint {

    private static final String UPLOAD_DIRECTORY = MegamindApp.INSTANCE.directory().resolve("stash").resolve("SKYPVP").resolve("plugins").toString();

    @Override
    public Object handle(Request request, Response response) {
        Logging.IMP.info("Received upload request.");
        try {
            if (!request.contentType().contains("multipart/form-data")) {
                response.status(400);
                return "Invalid content type. Please use 'multipart/form-data'.";
            }

            // Set up multipart config for file upload
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(""));

            Part uploadedFile = request.raw().getPart("file");
            if (uploadedFile == null) {
                response.status(400);
                return "File not provided.";
            }

            if (!uploadedFile.getContentType().equals("application/java-archive")) {
                response.status(400);
                return "Uploaded file is not a JAR.";
            }

            File targetFile = new File(UPLOAD_DIRECTORY, uploadedFile.getSubmittedFileName());
            try (InputStream inputStream = uploadedFile.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(targetFile)) {

                byte[] buffer = new byte[64 * 1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            response.status(200);
            return "File uploaded successfully.";

        } catch (Exception e) {
            Logging.IMP.stacktrace(e, "Error during file upload.");
            response.status(500);
            return "An error occurred while processing the upload.";
        }
    }
}
