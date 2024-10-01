package net.skykingdoms.api.backend.util.collection;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A concurrent set whose values may be stored as weak or soft references. If
 * the constructor is invoked with <code>refType</code> set to {@link #HARD},
 * this uses a JDK1.5 {@link ConcurrentHashMap} under the covers. Otherwise,
 * it uses a {@link ConcurrentReferenceHashMap}.
 *
 * @author Abe White
 */
public class ConcurrentReferenceHashSet<E> implements Set<E>, Serializable {

    /**
     * Hard reference marker.
     */
    public static final int HARD = 0;

    /**
     * Soft reference marker.
     */
    public static final int SOFT = 1;

    /**
     * Weak reference marker.
     */
    public static final int WEAK = 2;

    private static final Object DUMMY_VAL = new Object();

    private final Set<E> _set;

    /**
     * Construct a set with the given reference type.
     */
    public ConcurrentReferenceHashSet(int refType) {
        if (refType == HARD)
            _set = new SetFromMap<>(new ConcurrentHashMap<>(), DUMMY_VAL);
        else {
            int mapRefType = (refType == WEAK) ? ConcurrentReferenceHashMap.WEAK
                : ConcurrentReferenceHashMap.SOFT;
            _set = new SetFromMap<>(new ConcurrentReferenceHashMap<>
                (mapRefType, ConcurrentReferenceHashMap.STRONG), DUMMY_VAL);
        }
    }

    public boolean add(E obj) {
        return _set.add(obj);
    }

    public boolean addAll(@NotNull Collection<? extends E> collection) {
        return _set.addAll(collection);
    }

    public void clear() {
        _set.clear();
    }

    public boolean contains(Object obj) {
        return _set.contains(obj);
    }

    public boolean containsAll(Collection<?> coll) {
        return _set.containsAll(coll);
    }

    public boolean isEmpty() {
        return _set.isEmpty();
    }

    public Iterator<E> iterator() {
        return _set.iterator();
    }

    public boolean remove(Object obj) {
        return _set.remove(obj);
    }

    public boolean removeAll(Collection<?> coll) {
        return _set.removeAll(coll);
    }

    public boolean retainAll(Collection<?> coll) {
        return _set.retainAll(coll);
    }

    public int size() {
        return _set.size();
    }

    public Object[] toArray() {
        return _set.toArray();
    }

    public <T> T[] toArray(T[] arr) {
        return _set.toArray(arr);
    }

    public int hashCode() {
        return _set.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof ConcurrentReferenceHashSet)
            obj = ((ConcurrentReferenceHashSet) obj)._set;
        return _set.equals(obj);
    }
}