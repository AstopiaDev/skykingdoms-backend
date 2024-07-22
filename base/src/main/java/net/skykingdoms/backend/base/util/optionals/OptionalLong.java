package net.skykingdoms.backend.base.util.optionals;

import lombok.NonNull;

import java.util.NoSuchElementException;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.LongStream;

/**
 * Custom wrapper for optional long
 */
public final class OptionalLong {

    private static final OptionalLong EMPTY = new OptionalLong();

    private final boolean isPresent;
    private final long value;

    private OptionalLong() {
        this.isPresent = false;
        this.value = 0;
    }

    public static OptionalLong empty() {
        return EMPTY;
    }

    private OptionalLong(final long value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalLong of(final long value) {
        return new OptionalLong(value);
    }

    public long getAsLong() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }

        return value;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public boolean isEmpty() {
        return !isPresent;
    }

    public void ifPresent(final @NonNull LongConsumer action) {
        if (isPresent) {
            action.accept(value);
        }
    }

    public void ifPresentOrElse(final @NonNull LongConsumer action,
                                final @NonNull Runnable emptyAction) {
        if (isPresent) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    public LongStream stream() {
        if (isPresent) {
            return LongStream.of(value);
        } else {
            return LongStream.empty();
        }
    }

    public long orElse(final long other) {
        return isPresent ? value : other;
    }

    public long orElseGet(final @NonNull LongSupplier supplier) {
        return isPresent ? value : supplier.getAsLong();
    }

    public long orElseThrow() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public<X extends Throwable> long orElseThrow(final @NonNull Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OptionalLong)) {
            return false;
        }

        OptionalLong other = (OptionalLong) obj;
        return (isPresent && other.isPresent)
                ? value == other.value
                : isPresent == other.isPresent;
    }

    @Override
    public int hashCode() {
        return isPresent ? Long.hashCode(value) : 0;
    }

    @Override
    public String toString() {
        return isPresent
                ? String.format("OptionalLong[%s]", value)
                : "OptionalLong.empty";
    }
}
