package net.skykingdoms.backend.base.mysql.util;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
public final class TableBuilder {

    @NotNull final StringBuilder builder;
    boolean hasColumns;

    public TableBuilder(final @NonNull String name) {
        builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(name).append('(');
    }

    public @NotNull TableBuilder addLine(final @NonNull String column) {
        if (hasColumns) {
            builder.append(',');
        } else {
            hasColumns = true;
        }

        builder.append(column);

        return this;
    }

    @Override
    public @NotNull String toString() {
        return builder.append(')').toString();
    }
}
