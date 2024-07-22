package net.skykingdoms.backend.base.util.optionals;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

public final class Optional<T> {

    private static final Optional<?> EMPTY = new Optional<>();

    private final T value;

    private Optional() {
        this.value = null;
    }

    public static<T> Optional<T> empty() {
        @SuppressWarnings("unchecked")
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }

    private Optional(final T value) {
        this.value = Objects.requireNonNull(value);
    }

    public static <T> Optional<T> of(final T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> ofNullable(final T value) {
        return value == null ? empty() : of(value);
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }

        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public void ifPresent(final @NonNull Consumer<? super T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    public void ifPresentOrElse(final @NonNull Consumer<? super T> action,
                                final @NonNull  Runnable emptyAction) {
        if (value != null) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    public Optional<T> filter(final @NonNull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }


    public <U> Optional<U> map(final @NonNull Function<? super T, ? extends U> mapper) {
        return isPresent()
                ? Optional.ofNullable(mapper.apply(value))
                : empty();
    }

    public OptionalDouble mapToDouble(final @NonNull ToDoubleFunction<? super T> mapper) {
        return isPresent()
                ? OptionalDouble.of(mapper.applyAsDouble(value))
                : OptionalDouble.empty();
    }

    public OptionalLong mapToLong(final @NonNull ToLongFunction<? super T> mapper) {
        return isPresent()
                ? OptionalLong.of(mapper.applyAsLong(value))
                : OptionalLong.empty();
    }

    public OptionalInt mapToInt(final @NonNull ToIntFunction<? super T> mapper) {
        return isPresent()
                ? OptionalInt.of(mapper.applyAsInt(value))
                : OptionalInt.empty();
    }

    @SuppressWarnings("unchecked")
    public <U> Optional<U> flatMap(final @NonNull Function<? super T, ? extends Optional<? extends U>> mapper) {
        return isPresent()
                ? Objects.requireNonNull((Optional<U>) mapper.apply(value))
                : empty();
    }

    public Optional<T> or(final @NonNull Supplier<? extends Optional<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent()) {
            return this;
        } else {
            @SuppressWarnings("unchecked")
            Optional<T> r = (Optional<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    public Stream<T> stream() {
        if (!isPresent()) {
            return Stream.empty();
        } else {
            return Stream.of(value);
        }
    }

    public T orElse(final @Nullable T other) {
        return value != null ? value : other;
    }

    public T orElseGet(final @NonNull Supplier<? extends T> supplier) {
        return value != null ? value : supplier.get();
    }

    public T orElseThrow() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public <X extends Throwable> T orElseThrow(final @NonNull Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
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

        if (!(obj instanceof Optional)) {
            return false;
        }

        Optional<?> other = (Optional<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null
                ? String.format("Optional[%s]", value)
                : "Optional.empty";
    }
}