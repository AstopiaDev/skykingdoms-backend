package net.skykingdoms.backend.base.util.optionals;

import lombok.NonNull;

import java.util.NoSuchElementException;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Custom wrapper for optional integer
 */
public final class OptionalInt {

    private static final OptionalInt EMPTY = new OptionalInt();

    private final boolean isPresent;
    private final int value;

    private OptionalInt() {
        this.isPresent = false;
        this.value = 0;
    }

    public static OptionalInt empty() {
        return EMPTY;
    }

    private OptionalInt(final int value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalInt of(final int value) {
        return new OptionalInt(value);
    }

    public int getAsInt() {
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

    public void ifPresent(final @NonNull IntConsumer action) {
        if (isPresent) {
            action.accept(value);
        }
    }

    public void ifPresentOrElse(final @NonNull IntConsumer action,
                                final @NonNull Runnable emptyAction) {
        if (isPresent) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    public IntStream stream() {
        if (isPresent) {
            return IntStream.of(value);
        } else {
            return IntStream.empty();
        }
    }

    public int orElse(final int other) {
        return isPresent ? value : other;
    }

    public int orElseGet(final @NonNull IntSupplier supplier) {
        return isPresent ? value : supplier.getAsInt();
    }

    public int orElseThrow() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public<X extends Throwable> int orElseThrow(final @NonNull Supplier<? extends X> exceptionSupplier) throws X {
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

        if (!(obj instanceof OptionalInt)) {
            return false;
        }

        OptionalInt other = (OptionalInt) obj;
        return (isPresent && other.isPresent)
                ? value == other.value
                : isPresent == other.isPresent;
    }

    @Override
    public int hashCode() {
        return isPresent ? Integer.hashCode(value) : 0;
    }

    @Override
    public String toString() {
        return isPresent
                ? String.format("OptionalInt[%s]", value)
                : "OptionalInt.empty";
    }
}
