package net.skykingdoms.backend.base.util.time;

import lombok.NonNull;

import java.util.concurrent.TimeUnit;

public enum DurationField {

    NANO(TimeUnit.NANOSECONDS),
    MICRO(TimeUnit.MICROSECONDS),
    MILLI(TimeUnit.MILLISECONDS),

    SEC(TimeUnit.SECONDS),
    MIN(TimeUnit.MINUTES),
    HOUR(TimeUnit.HOURS),

    DAY(TimeUnit.DAYS),
    WEEK(TimeUnit.DAYS.toNanos(7)),
    MONTH(TimeUnit.DAYS.toNanos(30)),
    YEAR(TimeUnit.DAYS.toNanos(365));

    final long nanos;

    DurationField(final @NonNull TimeUnit unit) {
        this(unit.toNanos(1));
    }

    DurationField(final long nanos) {
        this.nanos = nanos;
    }

}
