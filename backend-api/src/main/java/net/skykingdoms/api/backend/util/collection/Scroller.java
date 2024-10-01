package net.skykingdoms.api.backend.util.collection;

import com.google.common.collect.Iterators;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * @author WhileInside
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("unchecked")
public class Scroller<T> {

    final int size;

    final Iterator<T> iterator;

    @Getter
    T selectedElement;
    @Getter
    int selectedVisibleRow;

    @Getter
    final T[] visibleElements;

    public Scroller(final int size,
                    final T... elements) {
        if (elements.length == 0) {
            throw new IllegalArgumentException();
        }

        this.size = size;

        this.iterator = Iterators.cycle(elements);
        this.visibleElements = (T[]) Array.newInstance(elements.getClass().getComponentType(), size);

        for (int i = 0; i < size; i++) {
            this.visibleElements[i] = iterator.next();
        }

        this.selectedElement = visibleElements[0];
    }

    public void scroll() {
        scroll(false);
    }

    public synchronized void scroll(boolean fullStep) {
        if (fullStep) {
            if (++selectedVisibleRow == size) {
                for (var i = 0; i < size; i++) {
                    visibleElements[i] = iterator.next();
                }

                selectedVisibleRow = 0;
            }

            selectedElement = visibleElements[selectedVisibleRow];
        } else {
            if (size / 2 == selectedVisibleRow) {
                System.arraycopy(visibleElements, 1, visibleElements, 0, size - 1);

                selectedElement = visibleElements[size - 1];
                visibleElements[size - 1] = iterator.next();
            } else {
                selectedElement = visibleElements[++selectedVisibleRow];
            }
        }
    }

}