package net.skykingdoms.backend.base.util.time;

import lombok.*;
import net.skykingdoms.backend.base.language.Language;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public final class Duration {

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FormatParams {

        /**
         * Time unit that is passed to method
         */
        public TimeUnit sourceUnit = TimeUnit.MILLISECONDS;

        /**
         * Locale for formatting every field
         */
        public DurationFormatter locale = DurationFormatter.EN;

        /**
         * Minimal time unit to display
         */
        public DurationField min = DurationField.NANO;

        /**
         * Maximal time unit to display
         */
        public DurationField max = DurationField.YEAR;

        /**
         * Fields in this set will be excluded from ret string
         */
        public final EnumSet<DurationField> exclude = EnumSet.of(DurationField.WEEK);

    }

    public static String format(final @NonNull LocalDateTime first,
                                final @Nullable LocalDateTime second,
                                @Nullable FormatParams formatParams) {

        if (formatParams == null) {
            formatParams = FormatParams.builder()
                    .sourceUnit(TimeUnit.MILLISECONDS)
                    .min(DurationField.MIN)
                    .max(DurationField.DAY)
                    .locale(DurationFormatter.EN)
                    .build();
        }

        val executionDateEpoch = ZonedDateTime.of(first, ZoneId.systemDefault()).toInstant().toEpochMilli();
        val expirationDateEpoch = second == null ? 0 : ZonedDateTime.of(second, ZoneId.systemDefault()).toInstant().toEpochMilli();

        return Duration.format(expirationDateEpoch - executionDateEpoch, formatParams);
    }

    public static String format(long duration, @NonNull FormatParams params) {
        long value = duration;

        StringBuilder builder = new StringBuilder();

        params.locale.start(builder);

        for (int i = params.max.ordinal(); i >= params.min.ordinal(); i--) {
            DurationField field = DurationField.values()[i];

            if (params.exclude.contains(field)) {
                continue;
            }

            long div = field.nanos / params.sourceUnit.toNanos(1);
            if (div == 0) {
                continue;
            }

            if (value >= div) {
                long fieldVal = value / div;
                value %= div;

                params.locale.append(builder, fieldVal, field);
            } else {
                params.locale.append(builder, 0, field);
            }
        }

        params.locale.finish(builder);

        String result = builder.toString();

        if (result.isEmpty()) {
            result = "N/A";
        }

        return result;
    }

    /**
     * 10000L mins -> "6 дней"<br/>
     * 100L mins -> "1 час 40 минут"
     */
    public static String ruWordsMin(long min) {
        return durationWordsMs(Language.RUSSIAN, TimeUnit.MINUTES.toMillis(min));
    }

    public static String ruWordsSec(long sec) {
        return durationWordsMs(Language.RUSSIAN, TimeUnit.SECONDS.toMillis(sec));
    }

    public static String ruWordsBetween(Temporal start, Temporal end) {
        return wordsBetween(Language.RUSSIAN, start, end);
    }

    public static String wordsBetween(Language language, Temporal start, Temporal end) {
        return durationWordsSec(language, java.time.Duration.between(start, end).getSeconds());
    }

    public static String ruWordsMs(long ms) {
        return durationWordsMs(Language.RUSSIAN, ms);
    }

    public static String durationShortMin(@NonNull Language type, long min) {
        return durationWordsMs(type, TimeUnit.MINUTES.toMillis(min));
    }

    public static String durationShortSec(@NonNull Language type, long sec) {
        return durationWordsMs(type, TimeUnit.SECONDS.toMillis(sec));
    }

    public static String durationShortMs(@NonNull Language type, long ms) {
        FormatParams params = new FormatParams();
        params.sourceUnit = TimeUnit.MILLISECONDS;
        params.locale = type != Language.RUSSIAN ? DurationFormatter.EN_SHORT : DurationFormatter.RU_SHORT;

        if (ms > TimeUnit.DAYS.toMillis(1)) {
            params.min = DurationField.HOUR;
        } else {
            if (ms > TimeUnit.MINUTES.toMillis(5)) {
                params.min = DurationField.MIN;
            } else {
                params.min = DurationField.SEC;
            }

            params.max = DurationField.HOUR;
        }

        return format(ms, params);
    }

    public static String durationWordsMin(@NonNull Language type, long min) {
        return durationWordsMs(type, TimeUnit.MINUTES.toMillis(min));
    }

    public static String durationWordsSec(@NonNull Language type, long sec) {
        return durationWordsMs(type, TimeUnit.SECONDS.toMillis(sec));
    }

    public static String durationWordsMs(@NonNull Language type, long ms) {
        FormatParams params = new FormatParams();
        params.sourceUnit = TimeUnit.MILLISECONDS;
        params.locale = type != Language.RUSSIAN ? DurationFormatter.EN : DurationFormatter.RU;

        if (ms > TimeUnit.DAYS.toMillis(1)) {
            params.min = DurationField.DAY;
        } else {
            if (ms > TimeUnit.MINUTES.toMillis(5)) {
                params.min = DurationField.MIN;
            } else {
                params.min = DurationField.SEC;
            }

            params.max = DurationField.HOUR;
        }

        return format(ms, params);
    }

    /**
     * 100L sec -> "01:40"
     */
    public static String simple(long sec) {
        FormatParams params = new FormatParams();
        params.sourceUnit = TimeUnit.SECONDS;
        params.locale = DurationFormatter.SIMPLE;
        params.min = DurationField.SEC;
        params.max = DurationField.MIN;

        return format(sec, params);
    }

}
