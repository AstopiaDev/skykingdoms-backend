package net.skykingdoms.backend.base.util.paged;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PagedList<E> extends AbstractPaging<List<E>> implements List<E> {

    public PagedList(List<E> backend, int limit) {
        super(backend, limit);
    }

    public static <E> Collector<E, ?, PagedList<E>> collector(int limit) {
        return Collectors.toCollection(() -> new PagedList<>(new LinkedList<>(), limit));
    }

    public int size() {
        return backend.size();
    }

    public boolean isEmpty() {
        return backend.isEmpty();
    }

    public boolean contains(Object o) {
        return backend.contains(o);
    }

    public Iterator<E> iterator() {
        return backend.iterator();
    }

    public Object[] toArray() {
        return backend.toArray();
    }

    public <T> T[] toArray(@NotNull T[] a) {
        return backend.toArray(a);
    }

    public boolean add(E e) {
        return backend.add(e);
    }

    public boolean remove(Object o) {
        return backend.remove(o);
    }

    public boolean containsAll(@NotNull Collection<?> c) {
        return backend.containsAll(c);
    }

    public boolean addAll(@NotNull Collection<? extends E> c) {
        return backend.addAll(c);
    }

    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        return backend.addAll(index, c);
    }

    public boolean removeAll(@NotNull Collection<?> c) {
        return backend.removeAll(c);
    }

    public boolean retainAll(@NotNull Collection<?> c) {
        return backend.retainAll(c);
    }

    public void replaceAll(UnaryOperator<E> operator) {
        backend.replaceAll(operator);
    }

    public void sort(Comparator<? super E> c) {
        backend.sort(c);
    }

    public void clear() {
        backend.clear();
    }

    @Override
    public boolean equals(Object o) {
        return backend.equals(o);
    }

    @Override
    public int hashCode() {
        return backend.hashCode();
    }

    public E get(int index) {
        return backend.get(index);
    }

    public E set(int index, E element) {
        return backend.set(index, element);
    }

    public void add(int index, E element) {
        backend.add(index, element);
    }

    public E remove(int index) {
        return backend.remove(index);
    }

    public int indexOf(Object o) {
        return backend.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return backend.lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        return backend.listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return backend.listIterator(index);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return backend.subList(fromIndex, toIndex);
    }

    public Spliterator<E> spliterator() {
        return backend.spliterator();
    }

    public boolean removeIf(Predicate<? super E> filter) {
        return backend.removeIf(filter);
    }

    public Stream<E> stream() {
        return backend.stream();
    }

    public Stream<E> parallelStream() {
        return backend.parallelStream();
    }

    public void forEach(Consumer<? super E> action) {
        backend.forEach(action);
    }
}
