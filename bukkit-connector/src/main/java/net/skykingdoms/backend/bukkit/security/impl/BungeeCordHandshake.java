package net.skykingdoms.backend.bukkit.security.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.bukkit.security.BukkitSecurityManager;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Encapsulates a BungeeCord "ip forwarding" handshake result.
 */
public class BungeeCordHandshake {

    /** The name of the auth token. */
    private static final String SECURITY_TOKEN_NAME = "security-token";

    /** The key used to define the name of properties in the handshake. */
    private static final String PROPERTY_NAME_KEY = "name";

    /** The key used to define the value of properties in the handshake. */
    private static final String PROPERTY_VALUE_KEY = "value";

    /** The maximum allowed length of the handshake. */
    private static final int HANDSHAKE_LENGTH_LIMIT = 2500;

    /** Shared Gson instance. */
    private static final Gson GSON = new Gson();

    /** The type of the property list in the handshake. */
    private static final Type PROPERTY_LIST_TYPE = new TypeToken<List<JsonObject>>(){}.getType();

    /**
     * Decodes a BungeeCord handshake, additionally ensuring it contains a
     * valid security token.
     *
     * @param handshake the handshake data
     * @return the handshake result
     */
    public static BungeeCordHandshake decodeAndVerify(final @NonNull String handshake) {
        try {
            return decodeAndVerify0(handshake);
        } catch (Exception e) {
            Logging.IMP.stacktrace(e, "Failed to decode handshake!");
            return new Fail(Fail.Reason.INVALID_HANDSHAKE, encodeBase64(handshake));
        }
    }

    private static BungeeCordHandshake decodeAndVerify0(final @NonNull String handshake) {
        if (handshake.length() > HANDSHAKE_LENGTH_LIMIT) {
            return new Fail(Fail.Reason.INVALID_HANDSHAKE, "handshake length " + handshake.length() + " is > " + HANDSHAKE_LENGTH_LIMIT);
        }

        val split = handshake.split("\00");
        if (split.length != 3 && split.length != 4) {
            return new Fail(Fail.Reason.INVALID_HANDSHAKE, encodeBase64(handshake));
        }

        val serverHostname = split[0];
        val socketAddressHostname = split[1];
        val uniqueId = UUID.fromString(split[2].replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));

        val connectionDescription = uniqueId + " @ " + encodeBase64(socketAddressHostname);

        if (split.length == 3) {
            return new Fail(Fail.Reason.NO_TOKEN, connectionDescription);
        }

        List<JsonObject> properties = new LinkedList<>(GSON.fromJson(split[3], PROPERTY_LIST_TYPE));
        if (properties.isEmpty()) {
            return new Fail(Fail.Reason.NO_TOKEN, connectionDescription);
        }

        String bungeeGuardToken = null;
        for (var iterator = properties.iterator(); iterator.hasNext(); ) {
            val property = iterator.next();
            if (property.get(PROPERTY_NAME_KEY).getAsString().equals(SECURITY_TOKEN_NAME)) {
                if (bungeeGuardToken != null) {
                    return new Fail(Fail.Reason.INCORRECT_TOKEN, connectionDescription + " - more than one token");
                }

                bungeeGuardToken = property.get(PROPERTY_VALUE_KEY).getAsString();
                iterator.remove();
            }
        }

        if (bungeeGuardToken == null) {
            return new Fail(Fail.Reason.NO_TOKEN, connectionDescription);
        }

        if (!BukkitSecurityManager.INSTANCE.isInitialized()) {
            return new Fail(Fail.Reason.INTERNAL_ERROR, connectionDescription);
        }

        if (!BukkitSecurityManager.INSTANCE.validate(bungeeGuardToken)) {
            return new Fail(Fail.Reason.INCORRECT_TOKEN, connectionDescription + " - " + encodeBase64(bungeeGuardToken));
        }

        val newPropertiesString = GSON.toJson(properties, PROPERTY_LIST_TYPE);

        return new Success(serverHostname, socketAddressHostname, uniqueId, newPropertiesString);
    }
    
    public static String encodeBase64(final @NonNull String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encapsulates a successful handshake.
     */
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class Success extends BungeeCordHandshake {

        String serverHostname;
        String socketAddressHostname;
        UUID uniqueId;
        String propertiesJson;

        Success(final @NonNull String serverHostname,
                final @NonNull String socketAddressHostname,
                final @NonNull UUID uniqueId,
                final @NonNull String propertiesJson) {
            this.serverHostname = serverHostname;
            this.socketAddressHostname = socketAddressHostname;
            this.uniqueId = uniqueId;
            this.propertiesJson = propertiesJson;
        }

        public String serverHostname() {
            return this.serverHostname;
        }

        public String socketAddressHostname() {
            return this.socketAddressHostname;
        }

        public UUID uniqueId() {
            return this.uniqueId;
        }

        public String propertiesJson() {
            return this.propertiesJson;
        }

        /**
         * Re-encodes this handshake to the format used by BungeeCord.
         *
         * @return an encoded string for the handshake
         */
        public String encode() {
            return this.serverHostname + "\00" + this.socketAddressHostname + "\00" + this.uniqueId + "\00" + this.propertiesJson;
        }
    }

    /**
     * Encapsulates an unsuccessful handshake.
     */
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class Fail extends BungeeCordHandshake {

        Reason reason;
        String connectionDescription;

        Fail(final @NonNull Reason reason,
             final @NonNull String connectionDescription) {
            this.reason = reason;
            this.connectionDescription = connectionDescription;
        }

        public Reason reason() {
            return this.reason;
        }

        public String describeConnection() {
            return this.connectionDescription;
        }

        public enum Reason {
            INVALID_HANDSHAKE, NO_TOKEN, INCORRECT_TOKEN, INTERNAL_ERROR
        }
    }

}