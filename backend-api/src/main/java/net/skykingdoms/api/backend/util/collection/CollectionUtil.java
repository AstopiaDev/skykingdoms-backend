package net.skykingdoms.api.backend.util.collection;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class CollectionUtil {

    public <K, V> SetMultimap<K, V> createConcurrentSetMultimap() {
        return Multimaps.newSetMultimap(
                createConcurrentMap(),
                CollectionUtil::createConcurrentSet
        );
    }

    public <E> Set<E> createConcurrentSet() {
        return ConcurrentHashMap.newKeySet();
    }

    public <K, V> Map<K, V> createConcurrentMap() {
        return new ConcurrentHashMap<>();
    }

    public <K extends Enum<K>, V> Map<K, V> createSynchronizedEnumMap(Class<K> cls) {
        return new EnumMap<>(cls);
    }

    public <E extends Enum<E>> Set<E> createSynchronizedEnumSet(Class<E> cls) {
        return Collections.synchronizedSet(EnumSet.noneOf(cls));
    }

    public <K, V> Map<K, V> createConcurrentWeakMap() {
        return new ConcurrentReferenceHashMap<>(
                ConcurrentReferenceHashMap.WEAK,
                ConcurrentReferenceHashMap.STRONG
        );
    }

    public <K, V> Map<K, V> createConcurrentSoftMap() {
        return new ConcurrentReferenceHashMap<>(
                ConcurrentReferenceHashMap.SOFT,
                ConcurrentReferenceHashMap.STRONG
        );
    }

    public <E> Set<E> createConcurrentWeakSet() {
        return new ConcurrentReferenceHashSet<>(ConcurrentReferenceHashMap.WEAK);
    }

    public <E> Set<E> createConcurrentSoftSet() {
        return new ConcurrentReferenceHashSet<>(ConcurrentReferenceHashMap.SOFT);
    }

}
