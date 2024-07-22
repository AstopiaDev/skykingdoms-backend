package net.skykingdoms.backend.neoprotect.util;

import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.skykingdoms.backend.base.util.collection.CollectionUtil;
import net.skykingdoms.backend.neoprotect.NeoProtectModule;
import net.skykingdoms.backend.neoprotect.dao.Backend;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.util.TextFormat;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@UtilityClass
public final class NeoProtectHttpUtil {

    private static final String baseURL = "https://api.neoprotect.net/v2";

    OkHttpClient client = new OkHttpClient();
    static { // todo: refactor
        client.setConnectTimeout(5, TimeUnit.SECONDS);
    }

    public ResponseWrapper request(final @NonNull NeoRequestType type,
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
        return defaultBuilder(NeoProtectModule.INSTANCE.getApi());
    }

    public Request.Builder defaultBuilder(final @NonNull String apiKey) {
        return new Request.Builder()
                .addHeader("accept", "*/*")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json");
    }

    public Collection<Backend> retrieveBackends() {
        Collection<Backend> ret = new HashSet<>();
        Logging.IMP.info(request(NeoRequestType.GET_BACKENDS, null, NeoProtectModule.INSTANCE.getServerId()).response.message());
        /*Logging.IMP.info(jsonObject.getAsString());*/
/*            val elements = backendgroup.getAsJsonArray("backends");
            for (val ele : elements) {
                val element = ele.getAsJsonObject();
                ret.add(new Backend(
                        element.get("id").getAsString(),
                        element.get("ipv4").getAsString(),
                        element.get("port").getAsInt(),
                        element.get("geyser").getAsBoolean()
                ));
            }*//*
        });*/
        return ret;
    }

    public String getEndpoint(final @NonNull NeoRequestType type,
                              final Object... values) {

        switch (type) {
            case POST_BACKEND: {
                return TextFormat.formatText("/gameshields/{}/backendGroups", values);
            }
            case GET_BACKENDS: {
                return TextFormat.formatText("/gameshields/{}/backendGroups/{}/backends", values);
            }
        }

        return null;
    }

}
