package net.skykingdoms.backend.base.mysql;

import com.google.gson.JsonObject;
import lombok.NonNull;
import net.skykingdoms.backend.base.util.optionals.Optional;
import net.skykingdoms.backend.base.util.optionals.OptionalDouble;
import net.skykingdoms.backend.base.util.optionals.OptionalInt;
import net.skykingdoms.backend.base.util.optionals.OptionalLong;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public final class BufferedRow extends LinkedHashMap<String, Object> {

    private @NotNull String formatError(final @NonNull Class<?> cls,
                                        final @NonNull String column) throws IllegalStateException {
        return "Column " + column + " with type " + cls.getSimpleName() + " not found in " + formatColumns();
    }

    private @NotNull String formatColumns() {
        return entrySet()
                .stream().map(entry -> entry.getKey() + " [" + entry.getValue().getClass().getSimpleName() + "]")
                .collect(Collectors.joining(", "));
    }

    private @NotNull <T> Optional<T> lookup(final @NonNull String column,
                                            final @NonNull Class<T> cls) {
        Object o = get(column);

        try {
            return Optional.ofNullable(cls.cast(o));
        } catch (ClassCastException e) {
            throw new IllegalStateException(formatError(cls, column));
        }
    }

    public @NotNull Optional<String> getString(final @NonNull String column) {
        return lookup(column, String.class);
    }

    public @NotNull OptionalInt getInt(final @NonNull String column) {
        return lookup(column, Number.class).mapToInt(Number::intValue);
    }

    public @NotNull Optional<JsonObject> getJson(final @NonNull String column) {
        return lookup(column, JsonObject.class);
    }

    public @NotNull OptionalLong getLong(final @NonNull String column) {
        return lookup(column, Number.class).mapToLong(Number::longValue);
    }

    @NotNull
    public OptionalDouble getDouble(final @NonNull String column) {
        return lookup(column, Number.class).mapToDouble(Number::doubleValue);
    }

    public boolean getBoolean(final @NonNull String column) {
        Object object = get(column);

        if (object instanceof Boolean) {
            return (boolean) object;
        } else if (object instanceof Number) {
            return ((Number) object).byteValue() == 1;
        } else if (object instanceof String) {
            return object.equals("true");
        }

        return false;
    }

    public @NotNull Optional<byte[]> getBlob(final @NonNull String column) {
        return lookup(column, byte[].class);
    }

    public @NotNull Optional<@NotNull InetAddress> getAddress(final @NonNull String column) {
        try {
            InetAddress address;
            Object obj = get(column);

            if (obj instanceof InetAddress) {
                address = (InetAddress) obj;
            } else if (obj instanceof String) {
                address = InetAddress.getByName((String) obj);
            } else if (obj instanceof byte[]) {
                address = InetAddress.getByAddress((byte[]) obj);
            } else if (obj instanceof Number) {
                int value = ((Number) obj).intValue();

                address = InetAddress.getByAddress(new byte[]{
                        (byte) ((value & 0xFF000000) >> 24),
                        (byte) ((value & 0xFF0000) >> 16),
                        (byte) ((value & 0xFF00) >> 8),
                        (byte) (value & 0xFF)
                });
            } else return Optional.empty();

            return Optional.of(address);
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    public @NotNull Optional<@NotNull LocalTime> getTime(final @NonNull String column) {
        return lookup(column, Time.class).map(Time::toLocalTime);
    }

    public @NotNull Optional<@NotNull LocalDateTime> getTimestamp(final @NonNull String column) {
        return lookup(column, Timestamp.class).map(Timestamp::toLocalDateTime);
    }

    public int getRequiredInt(final @NonNull String column) {
        return getInt(column).orElseThrow();
    }

    public byte[] getRequiredBlob(final @NonNull String column) {
        return getBlob(column).orElseThrow();
    }

    public double getRequiredDouble(final @NonNull String column) {
        return getDouble(column).orElseThrow();
    }

    public long getRequiredLong(final @NonNull String column) {
        return getLong(column).orElseThrow();
    }

    public @NotNull InetAddress getRequiredAddress(final @NonNull String column) {
        return getAddress(column).orElseThrow();
    }

    public @NotNull String getRequiredString(final @NonNull String column) {
        return getString(column).orElseThrow();
    }

    public @NotNull LocalTime getRequiredTime(final @NonNull String column) {
        return getTime(column).orElseThrow();
    }

    public @NotNull LocalDateTime getRequiredTimestamp(final @NonNull String column) {
        return getTimestamp(column).orElseThrow();
    }

}