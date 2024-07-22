package net.skykingdoms.backend.base.util.string;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.IntFunction;

public class ObjectLinkedBuf implements Iterable<Object> {

    public static final ObjectLinkedBuf EMPTY = new ObjectLinkedBuf() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public void clear() {
        }

        @Override
        public <T> T[] toArray(IntFunction<T[]> factory) {
            return factory.apply(0);
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> int toArray(T[] out) {
            return 0;
        }

        @Override
        public <T> int toArray(int offset, T[] out) {
            return 0;
        }

        @Override
        public @NotNull Iterator<Object> iterator() {
            return new Iterator<Object>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Object next() {
                    throw new IndexOutOfBoundsException();
                }
            };
        }

        @Override
        public @NotNull Reader reader() {
            return new Reader() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Object readNextObject() {
                    throw new IndexOutOfBoundsException();
                }
            };
        }

        @Override
        public @NotNull Writer writer() {
            return new Writer() {
                @Override
                public Writer writeNext(Object o) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Writer writeNext(Object... o) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    };

    private ObjectNode entrance;
    private ObjectNode exit;

    private int size;

    public ObjectLinkedBuf() {
    }

    public ObjectLinkedBuf(Object... values) {
        writer().writeNext(values);
    }

    public int size() {
        return size;
    }

    public Object getFirstObject() {
        return entrance == null ? null : entrance.element;
    }

    public <T> T getFirstElement() {
        return (T) getFirstObject();
    }

    public void clear() {
        entrance = exit = null;
        size = 0;
    }

    public <T> T[] toArray(IntFunction<T[]> factory) {
        T[] array = factory.apply(size);

        toArray(array);

        return array;
    }

    public Object[] toArray() {
        Object[] array = new Object[size];

        toArray(array);

        return array;
    }

    public <T> int toArray(T[] out) {
        return toArray(0, out);
    }

    @SuppressWarnings("unchecked")
    public <T> int toArray(int offset, T[] out) {
        if (entrance == null) {
            return 0;
        }

        int cnt = 0;
        ObjectNode value = entrance;

        do {
            out[offset + cnt++] = (T) value.element;
        } while ((value = value.next) != null);

        return cnt;
    }

    @NotNull
    @Override
    public Iterator<Object> iterator() {
        Reader reader = reader();

        return new Iterator<Object>() {
            @Override
            public boolean hasNext() {
                return reader.hasNext();
            }

            @Override
            public Object next() {
                return reader.readNextObject();
            }
        };
    }

    private static class ObjectNode {
        private final Object element;
        private ObjectNode next;

        public ObjectNode(Object element) {
            this.element = element;
        }

    }

    @NotNull
    public Reader reader() {
        return new Reader();
    }

    @NotNull
    public Writer writer() {
        return new Writer();
    }

    public class Reader {

        private ObjectNode node = entrance;

        public boolean hasNext() {
            return node != null;
        }

        public int readNextObjects(Object[] objects) {
            int cnt = 0;

            while (hasNext()) {
                Object o = node.element;
                node = node.next;
                objects[cnt++] = o;
            }

            return cnt;
        }

        public Object readNextObject() {
            if (node == null) {
                throw new IndexOutOfBoundsException();
            }

            Object o = node.element;
            node = node.next;
            return o;
        }

        public void skip() {
            node = node.next;
        }

        public <T> T readNext() {
            return (T) readNextObject();
        }

    }

    public class Writer {

        public Writer writeNext(Object o) {
            ObjectNode node = new ObjectNode(o);

            if (exit == null) {
                exit = entrance = node;
            } else {
                exit.next = node;
                exit = node;
            }

            size++;

            return this;
        }

        public Writer writeNext(Object... o) {
            for (Object element : o) {
                writeNext(element);
            }

            return this;
        }

    }
}
