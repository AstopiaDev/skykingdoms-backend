package net.skykingdoms.api.protocol.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public final class ArrayIterator<T> implements Iterator<T> {

    private final T[] array;

    private int position;

    public ArrayIterator(final T @NotNull [] array) {
        this.array = array;
    }

    @Override
    public boolean hasNext() {
        return position != array.length;
    }

    @Override
    public T next() {
        return array[position++];
    }
}
