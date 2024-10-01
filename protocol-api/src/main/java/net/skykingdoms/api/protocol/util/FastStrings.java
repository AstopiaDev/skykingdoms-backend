package net.skykingdoms.api.protocol.util;

import lombok.val;

public final class FastStrings {

    private FastStrings() {
        throw new UnsupportedOperationException();
    }

    private final static ThreadLocal<StringBuilder> CACHE = ThreadLocal.withInitial(StringBuilder::new);

    public static StringBuilder getEmptyBuilder() {
        val sb = CACHE.get();
        sb.setLength(0);

        return sb;
    }

}
