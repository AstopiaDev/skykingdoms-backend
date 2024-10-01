package net.skykingdoms.api.backend.util.collection;

import gnu.trove.map.hash.TCustomHashMap;

import java.util.Map;

public class CaseInsensitiveMap<V> extends TCustomHashMap<String, V> {

    public CaseInsensitiveMap() {
        super(CaseInsensitiveHashingStrategy.INSTANCE);
    }

    public CaseInsensitiveMap(Map<? extends String, ? extends V> map) {
        super(CaseInsensitiveHashingStrategy.INSTANCE, map);
    }
}