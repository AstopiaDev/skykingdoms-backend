package net.skykingdoms.api.protocol.type;

import java.util.Arrays;

public enum RemoteConnectedType {

    SERVER,
    PROXY,
    NODE,
    SERVICE
    ;

    final int mask = 1 << ordinal();

    public static final RemoteConnectedType[] VALUES = values();

    public static RemoteConnectedType byMask(final int mask) {
        return Arrays.stream(VALUES)
                .filter(v -> (v.mask & mask) != 0)
                .findFirst()
                .orElse(null);
    }
}