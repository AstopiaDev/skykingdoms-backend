package net.skykingdoms.backend.base.util.pair;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class Pair<A, B> {

    protected A a;
    protected B b;

    public Pair(final A a,
                final B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> Pair<A, B> newImmutablePair(final A a, final B b) {
        return new Immutable<>(a, b);
    }

    public static <A, B> Pair<A, B> newMutablePair(final A a, final B b) {
        return new Pair<>(a, b);
    }

    public final String format(final @NonNull String format) {
        return String.format(format, getFirst(), getSecond());
    }

    public boolean isImmutable() {
        return false;
    }

    public synchronized void setFirst(A first) {
        this.a = first;
    }

    public synchronized void setSecond(B second) {
        this.b = second;
    }

    public synchronized A getFirst() {
        return a;
    }

    public synchronized B getSecond() {
        return b;
    }

    public Pair<A, B> asImmutable() {
        return new Immutable<>(getFirst(), getSecond());
    }

    public Pair<A, B> asMutable() {
        return this;
    }

    public boolean isMember(final @Nullable Object object) {
        return object == null
                ? isFirstNull() || isSecondNull()
                : object.equals(getFirst()) || object.equals(getSecond());
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Pair))
            return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (isFirstNull() != pair.isFirstNull() || isSecondNull() != pair.isSecondNull()) {
            return false;
        }

        return (isFirstNull() || getFirst().equals(pair.getFirst()))
                && (isSecondNull() || getSecond().equals(pair.getSecond()));
    }

    @Override
    public int hashCode() {
        var hash = 1;
        hash = 31 * hash + (isFirstNull() ? 0 : getFirst().hashCode());
        hash = 31 * hash + (isSecondNull() ? 0 : getSecond().hashCode());

        return hash;
    }

    @Override
    public String toString() {
        return getFirst() + "; " + getSecond();
    }

    public boolean isFirstNull() {
        return getFirst() == null;
    }

    public boolean isSecondNull() {
        return getSecond() == null;
    }

    public void setFirstNull() {
        setFirst(null);
    }

    public void setSecondNull() {
        setSecond(null);
    }

    private static class Immutable<A, B> extends Pair<A, B> {
        public Immutable(final A first, final B second) {
            super(first, second);
        }

        @Override
        public boolean isImmutable() {
            return true;
        }

        @Override
        public void setFirst(final A first) {
            throw new IllegalStateException("Cannot modify immutable pair");
        }

        @Override
        public void setSecond(final B second) {
            throw new IllegalStateException("Cannot modify immutable pair");
        }

        @Override
        public Pair<A, B> asImmutable() {
            return this;
        }

        @Override
        public Pair<A, B> asMutable() {
            return new Pair<>(getFirst(), getSecond());
        }
    }

}
