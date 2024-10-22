package net.skykingdoms.backend.tcpshield.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.util.TextFormat;
import net.skykingdoms.backend.tcpshield.TcpShieldModule;
import net.skykingdoms.backend.tcpshield.dao.Backend;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@UtilityClass
public final class TcpShieldHttpUtil {

    private static final String baseURL = "https://api.neoprotect.net/v2";

    OkHttpClient client = new OkHttpClient();
    static { // todo: refactor
        client.setConnectTimeout(5, TimeUnit.SECONDS);
    }

    public ResponseWrapper request(final @NonNull TcpShieldRequestType type,
                                   final @Nullable RequestBody requestBody,
                                   final Object... value) {
        if (type.toString().startsWith("GET")) {
            return new ResponseWrapper(callRequest(defaultBuilder().url(baseURL + getEndpoint(type, value)).build()));
        } else if (type.toString().startsWith("POST")) {
            return new ResponseWrapper(callRequest(defaultBuilder().url(baseURL + getEndpoint(type, value)).post(requestBody).build()));
        } else {
            return new ResponseWrapper(callRequest(defaultBuilder().url(baseURL + getEndpoint(type, value)).delete().build()));
        }
    }

    public Response callRequest(final @NonNull Request request) {
        try {
            return client.newCall(request).execute();
        } catch (final Exception e) {
            Logging.IMP.stacktrace(e, "Exception raised on #callRequest() invocation!");
        }

        return null;
    }

    public Request.Builder defaultBuilder() {
        return defaultBuilder(TcpShieldModule.INSTANCE.getApi());
    }

    public Request.Builder defaultBuilder(final @NonNull String apiKey) {
        return new Request.Builder()
                .addHeader("accept", "*/*")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json");
    }

/*    public Collection<Backend> retrieveBackends() {
        Collection<Backend> ret = new HashSet<>();
        JsonArray backendGroupsArray = request(TcpShieldRequestType.GET_BACKENDS, null, TcpShieldModule.INSTANCE.getServerId()).getResponseBodyArray();
        for (val backendGroupElement : backendGroupsArray) {
            JsonObject backendGroup = backendGroupElement.getAsJsonObject();
            if (backendGroup.get("useDefault").getAsBoolean()) {
                TcpShieldModule.INSTANCE.setBackendGroupId(backendGroup.get("id").getAsString());
            }
            JsonArray backends = backendGroup.getAsJsonArray("backends");
            for (val backendElement : backends) {
                JsonObject backend = backendElement.getAsJsonObject();
                ret.add(new Backend(
                        backend.get("id").getAsString(),
                        backend.get("ipv4").getAsString(),
                        backend.get("port").getAsInt(),
                        backend.has("geyser") && !backend.get("geyser").isJsonNull() && backend.get("geyser").getAsBoolean()
                ));
            }
        }
        return ret;
    }*/

    public String getEndpoint(final @NonNull TcpShieldRequestType type,
                              final Object... values) {

        switch (type) {
            case POST_BACKEND: {
                return TextFormat.formatText("/gameshields/{}/backendGroups/{}/backends", values);
            }
            case GET_BACKENDS: {
                return TextFormat.formatText("/gameshields/{}/backendGroups", values);
            }
            case DELETE_BACKEND: {
                return TextFormat.formatText("/gameshields/{}/backendGroups/{}/backends/{}", values);
            }
        }

        return null;
    }

}
