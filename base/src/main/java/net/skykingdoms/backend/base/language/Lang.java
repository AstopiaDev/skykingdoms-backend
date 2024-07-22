package net.skykingdoms.backend.base.language;

import lombok.NonNull;

public final class Lang {

    private final StringBuilder builder;

    public Lang(final @NonNull String localizationKey) {
        this.builder = new StringBuilder("lang:").append(localizationKey);
    }

    public static Lang of(final @NonNull String localizationKey) {
        return new Lang(localizationKey);
    }

    public Lang set(final @NonNull String key, final Object value) {
        builder.append(':').append(key).append(':').append('{').append(value.toString()).append('}');
        return this;
    }

    public Lang setRaw(final @NonNull String key, Object value) {
        builder.append(':').append(key).append(':').append(value.toString());
        return this;
    }

    public Lang setAll(final @NonNull Object... objects) {
        if ((objects.length % 2) != 0) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < objects.length; i += 2) {
            set(objects[i].toString(), objects[i + 1]);
        }

        return this;
    }

    @Override
    public String toString() {
        return builder.append(';').toString();
    }

    public String compile() {
        return toString();
    }

}
