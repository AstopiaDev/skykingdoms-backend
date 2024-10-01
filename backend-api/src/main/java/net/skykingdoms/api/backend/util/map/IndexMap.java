package net.skykingdoms.api.backend.util.map;

import com.google.common.collect.SetMultimap;
import gnu.trove.TCollections;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class IndexMap<Instance> {

    final Map<Class<?>, Index> indexes = CollectionUtil.createConcurrentMap();

    public static <Instance> CachingIndexMap<Instance> newCachingMap(long aliveTime, TimeUnit unit) {
        return new CachingIndexMap<>(aliveTime, unit);
    }

    @Override
    public String toString() {
        return "[" + indexes.values().stream().map(Index::toString).collect(Collectors.joining(", ")) + "]";
    }

    public static <Instance> IndexMap<Instance> newMap() {
        return new BasicIndexMap<>();
    }

    public abstract Set<Instance> values();

    public boolean isEmpty() {
        return indexes.isEmpty();
    }

    public <T> IndexMap<Instance> registerIndex(Class<T> cls, Function<Instance, ?> fun) {
        indexes.put(cls, new HashIndex<>(fun));

        return this;
    }

    public <T> IndexMap<Instance> registerMultiIndex(Class<T> cls, Function<Instance, Collection<?>> fun) {
        indexes.put(cls, new MultiIndex<>(fun));

        return this;
    }

    public IndexMap<Instance> registerIntIndex(ToIntFunction<Instance> fun) {
        indexes.put(int.class, new IntIndex<>(fun));

        return this;
    }

    public void unregisterIndex(Class<?> cls) {
        indexes.remove(cls);
    }

    public <T> Instance get(Class<T> cls, T object) {
        Index<Instance> index = indexes.get(cls);

        if (index instanceof ObjectIndex) {
            return ((ObjectIndex<Instance>) index).get(object);
        }

        throw new IllegalStateException("Can't find index for " + cls.getSimpleName());
    }

    public Instance get(int i) {
        Index<Instance> index = indexes.get(int.class);

        if (index instanceof IntIndex) {
            return ((IntIndex<Instance>) index).get(i);
        }

        throw new IllegalStateException("Can't find int index");
    }

    public <T> Set<Instance> getAll(Class<T> cls, T object) {
        Index<Instance> index = indexes.get(cls);

        if (index instanceof MultiIndex) {
            return ((MultiIndex<Instance>) index).get(object);
        }

        throw new IllegalStateException("Can't find multi index for " + object.getClass().getSimpleName());
    }

    public boolean contains(int i) {
        return get(i) != null;
    }

    public abstract int size();

    public <T> boolean contains(Class<T> cls, T object) {
        Index<Instance> index = indexes.get(cls);

        if (index instanceof ObjectIndex) {
            return ((ObjectIndex<Instance>) index).get(object) != null;
        } else if (index instanceof MultiIndex) {
            return !((MultiIndex<Instance>) index).get(object).isEmpty();
        } else {
            throw new IllegalStateException("Can't find index for " + object.getClass().getSimpleName());
        }
    }

    public void store(Instance instance) {
        for (Index<Instance> index : indexes.values()) {
            index.store(instance);
        }
    }

    public void remove(Instance instance) {
        for (Index<Instance> index : indexes.values()) {
            index.remove(instance);
        }
    }

    public void clear() {
        indexes.clear();
    }

    abstract static class Index<Instance> {

        abstract void store(Instance instance);

        abstract void remove(Instance instance);

    }

    @RequiredArgsConstructor
    static class IntIndex<Instance> extends Index<Instance> {

        final ToIntFunction<Instance> fun;
        final TIntObjectMap<Instance> storage = TCollections.synchronizedMap(new TIntObjectHashMap<>());

        @Override
        void store(Instance instance) {
            storage.put(fun.applyAsInt(instance), instance);
        }

        @Override
        void remove(Instance instance) {
            storage.remove(fun.applyAsInt(instance));
        }

        Instance get(int i) {
            return storage.get(i);
        }

        @Override
        public String toString() {
            return storage.toString();
        }
    }

    @RequiredArgsConstructor
    abstract static class ObjectIndex<Instance> extends Index<Instance> {
        final Function<Instance, ?> fun;

        abstract Instance get(Object object);

    }

    @RequiredArgsConstructor
    static class MultiIndex<Instance> extends Index<Instance> {
        final Function<Instance, Collection<?>> fun;
        final SetMultimap<Object, Instance> storage = CollectionUtil.createConcurrentSetMultimap();

        @Override
        void store(Instance instance) {
            for (Object key : fun.apply(instance)) {
                storage.put(key, instance);
            }
        }

        @Override
        void remove(Instance instance) {
            for (Object key : fun.apply(instance)) {
                storage.remove(key, instance);
            }
        }

        Set<Instance> get(Object object) {
            return storage.get(object);
        }

        @Override
        public String toString() {
            return storage.toString();
        }

    }

    static class HashIndex<Instance> extends ObjectIndex<Instance> {
        private final Map<Object, Instance> storage = Collections.synchronizedMap(new HashMap<>());

        HashIndex(Function<Instance, ?> transfer) {
            super(transfer);
        }

        @Override
        void store(Instance instance) {
            storage.put(fun.apply(instance), instance);
        }

        @Override
        void remove(Instance instance) {
            storage.remove(fun.apply(instance), instance);
        }

        @Override
        Instance get(Object object) {
            return storage.get(object);
        }

        @Override
        public String toString() {
            return storage.toString();
        }

    }

}
