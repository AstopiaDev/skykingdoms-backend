package net.skykingdoms.backend.base.data.skin.mojang;

import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.skykingdoms.backend.base.data.skin.Skin;
import net.skykingdoms.backend.base.data.skin.exception.MojangCooldownException;
import net.skykingdoms.backend.base.data.skin.exception.ProfileNotFoundException;
import net.skykingdoms.backend.base.data.skin.exception.SkinException;
import net.skykingdoms.backend.base.data.skin.exception.SkinNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@UtilityClass
public final class MojangApi {

    String UUID_BY_NAME_STRING = "https://api.mojang.com/users/profiles/minecraft/%s";
    String SKIN_BY_UUID_STRING = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    private String readURL(final @NonNull String url) throws SkinException {
        try {
            val connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mexish_stepdad");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);

            if (connection.getResponseCode() == 429) {
                throw new MojangCooldownException();
            }

            val output = new StringBuilder();

            try (val in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                while (in.ready()) {
                    output.append(in.readLine());
                }
            }

            return output.toString();
        } catch (IOException exception) {
            return null;
        }
    }

    public Skin getSkinTextures(final @NonNull String name,
                                final @NonNull UUID playerUniqueId) throws SkinException {

        val skinResponse = readURL(String.format(SKIN_BY_UUID_STRING, playerUniqueId));

        if (skinResponse == null || skinResponse.isEmpty()) {
            throw new SkinNotFoundException();
        }

        val propertiesArray = JsonParser.parseString(skinResponse).getAsJsonObject().get("properties").getAsJsonArray();

        if (propertiesArray.isEmpty()) {
            throw new SkinNotFoundException();
        }

        val textureProperty = propertiesArray.get(0).getAsJsonObject();

        val value = textureProperty.get("value").getAsString();
        val signature = textureProperty.get("signature").getAsString();

        return new Skin(name, playerUniqueId.toString(), value, signature, System.currentTimeMillis());
    }

    public Skin getSkinTextures(final @NonNull String name) throws SkinException {
        val uuidResponse = readURL(String.format(UUID_BY_NAME_STRING, name));

        if (uuidResponse == null || uuidResponse.isEmpty()) {
            throw new ProfileNotFoundException();
        }

        return getSkinTextures(name, UUID.fromString(JsonParser.parseString(uuidResponse)
                .getAsJsonObject().get("id").getAsString().replaceAll(
                        "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                        "$1-$2-$3-$4-$5")));
    }

}
