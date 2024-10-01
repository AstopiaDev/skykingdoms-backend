package net.skykingdoms.api.backend.util.collection;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class SetFromMap<E, V> extends AbstractSet<E> implements Serializable {

    private final Map<E, ? super V> map;
    private final Set<E> keySet;

    private final V dummyValue;

    public SetFromMap(final Map<E, ? super V> map, final V dummyValue) {
        super();
        if (map == null) {
            throw new NullPointerException("The map must not be null");
        }

        this.map = map;
        this.keySet = map.keySet();

        this.dummyValue = dummyValue;
    }

    @Override
    public int size() {
        return keySet.size();
    }

    @Override
    public boolean isEmpty() {
        return keySet.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return keySet.iterator();
    }

    @Override
    public boolean contains(@NotNull Object obj) {
        return keySet.contains(obj);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> coll) {
        return keySet.containsAll(coll);
    }

    @Override
    public boolean add(E obj) {
        return map.put(obj, dummyValue) == null;
    }

    @Override
    public boolean remove(final Object obj) {
        return map.remove(obj) != null;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        return keySet.removeIf(filter);
    }

    @Override
    public boolean removeAll(final @NotNull Collection<?> coll) {
        return keySet.removeAll(coll);
    }

    @Override
    public boolean retainAll(final @NotNull Collection<?> coll) {
        return keySet.retainAll(coll);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public Stream<E> stream() {
        return keySet.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        return keySet.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        keySet.forEach(action);
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        return keySet.toArray(array);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || keySet.equals(obj);
    }

    @Override
    public int hashCode() {
        return map.keySet().hashCode();
    }

}



























































