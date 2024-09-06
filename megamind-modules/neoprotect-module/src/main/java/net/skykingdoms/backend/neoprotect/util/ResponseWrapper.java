package net.skykingdoms.backend.neoprotect.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Response;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.protocol.logging.Logging;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

public final class ResponseWrapper {

    JsonObject jsonObject;

    String responseBody;
    @Getter
    Response response;
    int code;

    public ResponseWrapper(final @NonNull Response response) {
        this.responseBody = getBody(response);
        this.response = response;
        this.code = getCode();
    }

    private String getBody(final @NonNull Response response) {
        try (val body = response.body()) {
            return body.string();
        } catch (NullPointerException | SocketTimeoutException ignored) {
            return "{}";
        } catch (IOException ex) {
            Logging.IMP.stacktrace(ex, "Exception raised at #getBody() invocation!");
            return null;
        }
    }

    public boolean checkCode(final int code) {
        return Objects.equals(this.code, code);
    }

    public int getCode() {
        try {
            return response.code();
        } catch (Exception ex) {
            return -1;
        }
    }

    public JsonObject getResponseBodyObject() {
        try {
            return JsonParser.parseString(responseBody).getAsJsonObject();
        } catch (Exception exc) {
            Logging.IMP.stacktrace(exc, "Exception raised at #getResponseBodyObject() invocation!");
        }
        return new JsonObject();
    }

    public JsonArray getResponseBodyArray() {
        try {
            return JsonParser.parseString(responseBody).getAsJsonArray();
        } catch (final Exception e) {
            Logging.IMP.stacktrace(e, "Exception raised at #getResponseBodyArray() invocation!");
        }
        return new JsonArray();
    }

    public final String getString(final @NonNull String key) {
        return jsonObject.get(key).getAsString();
    }
}