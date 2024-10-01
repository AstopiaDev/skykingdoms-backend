package net.skykingdoms.api.backend.util.collection;

import gnu.trove.strategy.HashingStrategy;

import java.util.Locale;

class CaseInsensitiveHashingStrategy implements HashingStrategy<String> {

    static final CaseInsensitiveHashingStrategy INSTANCE = new CaseInsensitiveHashingStrategy();

    @Override
    public int computeHashCode(String object) {
        return object.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public boolean equals(String o1, String o2) {
        return o1.equalsIgnoreCase(o2);
    }
}