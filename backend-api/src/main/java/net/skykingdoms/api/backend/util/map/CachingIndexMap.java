package net.skykingdoms.api.backend.util.map;

import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("unchecked")
public class CachingIndexMap<Instance> extends IndexMap<Instance> {

    Entry<Instance> oldestEntry;

    final long aliveTime;
    final Set<Entry<Instance>> entries = CollectionUtil.createConcurrentSet();

    public CachingIndexMap(long aliveTime, TimeUnit unit) {
        this.aliveTime = unit.toNanos(aliveTime);
    }

    @Override
    public Set<Instance> values() {
        return entries.stream().map(entry -> entry.value).collect(Collectors.toSet());
    }

    @Override
    public <T> CachingIndexMap<Instance> registerIndex(Class<T> cls, Function<Instance, ?> fun) {
        indexes.put(cls, new TempHashIndex<>(this, fun));

        return this;
    }

    @Override
    public <T> CachingIndexMap<Instance> registerMultiIndex(Class<T> cls, Function<Instance, Collection<?>> fun) {
        throw new UnsupportedOperationException("MultiIndex not supported in temporary map");
    }

    @Override
    public CachingIndexMap<Instance> registerIntIndex(ToIntFunction<Instance> fun) {
        indexes.put(int.class, new TempIntIndex<>(this, fun));

        return this;
    }

    @Override
    public <T> Instance get(Class<T> cls, T object) {
        Index<Instance> index = indexes.get(cls);

        if (index instanceof TempObjectIndex) {
            Entry<Instance> entry = ((TempObjectIndex<Instance>) index).get(object);

            if (entry == null) {
                return null;
            }

            entry.timestamp = System.nanoTime();

            return entry.value;
        }

        throw new IllegalStateException("Can't find index for " + object.getClass().getSimpleName());
    }

    @Override
    public Instance get(int i) {
        Index<Instance> index = indexes.get(int.class);

        if (index instanceof TempIntIndex) {
            Entry<Instance> entry = ((TempIntIndex<Instance>) index).get(i);

            if (entry == null) {
                return null;
            }

            entry.timestamp = System.nanoTime();

            return entry.value;
        }

        throw new IllegalStateException("Can't find int index");
    }

    @Override
    public void store(Instance instance) {
        Entry<Instance> entry = new Entry<>(System.nanoTime(), instance);

        entries.add(entry);

        for (Index<Entry<Instance>> index : indexes.values()) {
            index.store(entry);
        }
    }

    @Override
    public <T> Set<Instance> getAll(Class<T> cls, T object) {
        return Collections.singleton(get(cls, object));
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void remove(Instance instance) {
        Entry<Instance> entry = new Entry<>(-1, instance);

        if (entries.remove(entry)) {
            removeEntry(entry);
        }
    }

    private void removeEntry(Entry<Instance> entry) {
        if (oldestEntry != null && oldestEntry.value == entry.value) {
            oldestEntry = null;
        }

        for (Index<Entry<Instance>> index : indexes.values()) {
            index.remove(entry);
        }
    }

    public void cleanUp() {
        long now = System.nanoTime();

        boolean needCleanup = oldestEntry == null || oldestEntry.timestamp + aliveTime < now;

        if (needCleanup) {
            AtomicReference<Entry<Instance>> oldestEntry = new AtomicReference<>();

            entries.removeIf(entry -> {
                if (entry.timestamp + aliveTime < now) {
                    removeEntry(entry);

                    return true;
                }

                Entry<Instance> oldest = oldestEntry.get();

                if (oldest == null || oldest.timestamp > entry.timestamp) {
                    oldestEntry.set(entry);
                }

                return false;
            });

            this.oldestEntry = oldestEntry.get();
        }
    }

    @ToString(exclude = "timestamp")
    @EqualsAndHashCode(exclude = "timestamp")
    @AllArgsConstructor
    static class Entry<Instance> {

        private long timestamp;
        private final Instance value;

    }

    @RequiredArgsConstructor
    static class TempIntIndex<Instance> extends Index<Entry<Instance>> {

        final CachingIndexMap<Instance> map;
        final ToIntFunction<Instance> fun;
        final TIntObjectMap<Entry<Instance>> storage = TCollections.synchronizedMap(new TIntObjectHashMap<>());

        @Override
        void store(Entry<Instance> instance) {
            storage.put(fun.applyAsInt(instance.value), instance);
        }

        @Override
        void remove(Entry<Instance> instance) {
            storage.remove(fun.applyAsInt(instance.value));
        }

        Entry<Instance> get(int i) {
            return storage.get(i);
        }

    }

    @RequiredArgsConstructor
    abstract static class TempObjectIndex<Instance> extends Index<Entry<Instance>> {
        final CachingIndexMap<Instance> map;
        final Function<Instance, ?> fun;

        abstract Entry<Instance> get(Object object);
    }

    static class TempHashIndex<Instance> extends TempObjectIndex<Instance> {
        private final Map<Object, Entry<Instance>> storage = Collections.synchronizedMap(new HashMap<>());

        public TempHashIndex(CachingIndexMap<Instance> map, Function<Instance, ?> fun) {
            super(map, fun);
        }

        @Override
        void store(Entry<Instance> instance) {
            storage.put(fun.apply(instance.value), instance);
        }

        @Override
        void remove(Entry<Instance> instance) {
            storage.remove(fun.apply(instance.value));
        }

        Entry<Instance> get(Object object) {
            return storage.get(object);
        }

    }

}
