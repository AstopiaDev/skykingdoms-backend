package net.tcpshield.tcpshieldapi.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.tcpshield.tcpshieldapi.deserializer.DateDeserializer;
import net.tcpshield.tcpshieldapi.exception.APIConnectionException;
import net.tcpshield.tcpshieldapi.exception.status.NoPermissionException;
import net.tcpshield.tcpshieldapi.exception.status.NotFoundException;

import okhttp3.*;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestClient {

    private final Pattern HTTP_RESPONSE_CODE_EXCEPTION_PATTERN = Pattern.compile("^Server returned HTTP response code: (\\d+) for URL: .+$");
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();

    public RestClient(String apiKey) {
        this.apiKey = apiKey;

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new DateDeserializer());

        mapper.registerModule(module);
    }

    public <T> T makeRequest(RestRequest<T> restRequest) {
        RestResponse<T> response = null;
        int statusCode = -1;
        try {
            response = internalRequest(restRequest);
        } catch (IOException e) {
            String msg = e.getMessage();
            Matcher matcher = HTTP_RESPONSE_CODE_EXCEPTION_PATTERN.matcher(msg);

            if (!matcher.find()) throw new APIConnectionException(e);

            statusCode = Integer.parseInt(matcher.group(1));
        }

        if (statusCode != 0 && response != null)
            statusCode = response.getStatusCode();

        switch (statusCode) {
            case 403:
                throw new NoPermissionException();
            case 404:
                throw new NotFoundException();
            default:
                if (response == null)
                    throw new APIConnectionException("Response code unknown: " + statusCode + "; for request to " + restRequest.getURL());

                return response.getData();
        }
    }

    private <T> RestResponse<T> internalRequest(RestRequest<T> request) throws IOException {
        String data = toJson(request.getData());

        RequestBody body = data == null ? null : RequestBody.create(MediaType.parse("application/json; charset=utf-8"), data);

        Request.Builder requestBuilder = new Request.Builder()
                .url(request.getURL())
                .addHeader("X-API-Key", apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Content-Language", "en-US");

        switch (request.getRequestType()) {
            case PATCH:
                requestBuilder.patch(body);
                break;
            case POST:
                requestBuilder.post(body);
                break;
            case GET:
                requestBuilder.get();
                break;
            case DELETE:
                requestBuilder.delete(body);
                break;
        }

        Request httpRequest = requestBuilder.build();
        Response response = client.newCall(httpRequest).execute();
        try (ResponseBody responseBody = response.body()) {
            int statusCode = response.code();

            T parsed = parseJson(responseBody.string(), request.getResponseClass());
            return new RestResponse<>(statusCode, parsed);
        }
    }

    private String toJson(Object data) throws JsonProcessingException {
        if (data == null) return null;

        return mapper.writeValueAsString(data);
    }

    private <T> T parseJson(String data, Class<T> targetClass) throws IOException {
        if (targetClass == null) return null;

        return mapper.readValue(data, targetClass);
    }
}
