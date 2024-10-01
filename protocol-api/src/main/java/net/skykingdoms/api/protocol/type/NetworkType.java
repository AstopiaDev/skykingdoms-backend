package net.skykingdoms.api.protocol.type;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Enum representing type of network. It is used
 * to split development server and production server.
 *
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum NetworkType {

    /**
     * A network where players do be gaming
     */
    PRODUCTION("prod"),

    /**
     * A network where
     */
    DEVELOPMENT("dev");

    String prefix;

    public static NetworkType fromName(String name, NetworkType defaultValue) {
        for (NetworkType type : VALUES) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return defaultValue;
    }

    public static final NetworkType[] VALUES = NetworkType.values();
}
