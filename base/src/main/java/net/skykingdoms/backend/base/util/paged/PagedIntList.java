package net.skykingdoms.backend.base.util.paged;

import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.procedure.TIntProcedure;

import java.util.Collection;
import java.util.Random;

public final class PagedIntList extends AbstractPaging<TIntList> implements TIntList {

    public PagedIntList(TIntList backend, int limit) {
        super(backend, limit);
    }

    @Override
    public int getNoEntryValue() {
        return backend.getNoEntryValue();
    }

    @Override
    public int size() {
        return backend.size();
    }

    @Override
    public boolean isEmpty() {
        return backend.isEmpty();
    }

    @Override
    public boolean add(int i) {
        return backend.add(i);
    }

    @Override
    public void add(int[] ints) {
        backend.add(ints);
    }

    @Override
    public void add(int[] ints, int i, int i1) {
        backend.add(ints, i, i1);
    }

    @Override
    public void insert(int i, int i1) {
        backend.insert(i, i1);
    }

    @Override
    public void insert(int i, int[] ints) {
        backend.insert(i, ints);
    }

    @Override
    public void insert(int i, int[] ints, int i1, int i2) {
        backend.insert(i, ints, i1, i2);
    }

    @Override
    public int get(int i) {
        return backend.get(i);
    }

    @Override
    public int set(int i, int i1) {
        return backend.set(i, i1);
    }

    @Override
    public void set(int i, int[] ints) {
        backend.set(i, ints);
    }

    @Override
    public void set(int i, int[] ints, int i1, int i2) {
        backend.set(i, ints, i1, i2);
    }

    @Override
    public int replace(int i, int i1) {
        return backend.replace(i, i1);
    }

    @Override
    public void clear() {
        backend.clear();
    }

    @Override
    public boolean remove(int i) {
        return backend.remove(i);
    }

    @Override
    public int removeAt(int i) {
        return backend.removeAt(i);
    }

    @Override
    public void remove(int i, int i1) {
        backend.remove(i, i1);
    }

    @Override
    public void transformValues(TIntFunction tIntFunction) {
        backend.transformValues(tIntFunction);
    }

    @Override
    public void reverse() {
        backend.reverse();
    }

    @Override
    public void reverse(int i, int i1) {
        backend.reverse(i, i1);
    }

    @Override
    public void shuffle(Random random) {
        backend.shuffle(random);
    }

    @Override
    public TIntList subList(int i, int i1) {
        return backend.subList(i, i1);
    }

    @Override
    public int hashCode() {
        return backend.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return backend.equals(obj);
    }

    @Override
    public String toString() {
        return backend.toString();
    }

    @Override
    public int[] toArray() {
        return backend.toArray();
    }

    @Override
    public int[] toArray(int i, int i1) {
        return backend.toArray(i, i1);
    }

    @Override
    public int[] toArray(int[] ints) {
        return backend.toArray(ints);
    }

    @Override
    public int[] toArray(int[] ints, int i, int i1) {
        return backend.toArray(ints, i, i1);
    }

    @Override
    public int[] toArray(int[] ints, int i, int i1, int i2) {
        return backend.toArray(ints, i, i1, i2);
    }

    @Override
    public boolean forEach(TIntProcedure tIntProcedure) {
        return backend.forEach(tIntProcedure);
    }

    @Override
    public boolean forEachDescending(TIntProcedure tIntProcedure) {
        return backend.forEachDescending(tIntProcedure);
    }

    @Override
    public void sort() {
        backend.sort();
    }

    @Override
    public void sort(int i, int i1) {
        backend.sort(i, i1);
    }

    @Override
    public void fill(int i) {
        backend.fill(i);
    }

    @Override
    public void fill(int i, int i1, int i2) {
        backend.fill(i, i1, i2);
    }

    @Override
    public int binarySearch(int i) {
        return backend.binarySearch(i);
    }

    @Override
    public int binarySearch(int i, int i1, int i2) {
        return backend.binarySearch(i, i1, i2);
    }

    @Override
    public int indexOf(int i) {
        return backend.indexOf(i);
    }

    @Override
    public int indexOf(int i, int i1) {
        return backend.indexOf(i, i1);
    }

    @Override
    public int lastIndexOf(int i) {
        return backend.lastIndexOf(i);
    }

    @Override
    public int lastIndexOf(int i, int i1) {
        return backend.lastIndexOf(i, i1);
    }

    @Override
    public boolean contains(int i) {
        return backend.contains(i);
    }

    @Override
    public TIntList grep(TIntProcedure tIntProcedure) {
        return backend.grep(tIntProcedure);
    }

    @Override
    public TIntList inverseGrep(TIntProcedure tIntProcedure) {
        return backend.inverseGrep(tIntProcedure);
    }

    @Override
    public int max() {
        return backend.max();
    }

    @Override
    public int min() {
        return backend.min();
    }

    @Override
    public int sum() {
        return backend.sum();
    }

    @Override
    public TIntIterator iterator() {
        return backend.iterator();
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return backend.containsAll(collection);
    }

    @Override
    public boolean containsAll(TIntCollection tIntCollection) {
        return backend.containsAll(tIntCollection);
    }

    @Override
    public boolean containsAll(int[] ints) {
        return backend.containsAll(ints);
    }

    @Override
    public boolean addAll(Collection<? extends Integer> collection) {
        return backend.addAll(collection);
    }

    @Override
    public boolean addAll(TIntCollection tIntCollection) {
        return backend.addAll(tIntCollection);
    }

    @Override
    public boolean addAll(int[] ints) {
        return backend.addAll(ints);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return backend.retainAll(collection);
    }

    @Override
    public boolean retainAll(TIntCollection tIntCollection) {
        return backend.retainAll(tIntCollection);
    }

    @Override
    public boolean retainAll(int[] ints) {
        return backend.retainAll(ints);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return backend.removeAll(collection);
    }

    @Override
    public boolean removeAll(TIntCollection tIntCollection) {
        return backend.removeAll(tIntCollection);
    }

    @Override
    public boolean removeAll(int[] ints) {
        return backend.removeAll(ints);
    }
}
