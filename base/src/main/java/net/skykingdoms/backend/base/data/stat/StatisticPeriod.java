package net.skykingdoms.backend.base.data.stat;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.skykingdoms.backend.base.language.Language;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

@RequiredArgsConstructor
@Getter
public enum StatisticPeriod {

    ALL,
    MONTH {
        @Override
        public int getFromDay() {
            LocalDate firstMonthDate = LocalDate.now().withDayOfMonth(1);

            return (int) firstMonthDate.toEpochDay();
        }

        @Override
        public long getDurationUntilEnd() {
            val endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
            return Duration.between(LocalDateTime.now(), endOfMonth).toMillis();
        }
    },
    WEEK {
        @Override
        public int getFromDay() {
            LocalDateTime time = LocalDateTime.now();
            LocalDate date = time.toLocalDate();

            if (time.getDayOfWeek() == DayOfWeek.MONDAY) {
                if (time.isAfter(time.withHour(12).withMinute(0))) {
                    return (int) date.toEpochDay();
                } else {
                    return (int) date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                            .toEpochDay();
                }
            }

            return (int) date.with(DayOfWeek.MONDAY).toEpochDay();
        }

        @Override
        public long getDurationUntilEnd() {
            val endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).atTime(LocalTime.MAX);
            return Duration.between(LocalDateTime.now(), endOfWeek).toMillis();
        }
    },
    TODAY {
        @Override
        public int getFromDay() {
            return (int) LocalDate.now().toEpochDay();
        }

        @Override
        public long getDurationUntilEnd() {
            val now = LocalDateTime.now();
            // (23:59:59.999999999)
            val endOfDay = now.with(LocalTime.MAX);
            return Duration.between(now, endOfDay).toMillis();
        }
    };

    public String getDisplayName(final @NonNull Language language) {
        return language.getMessage(false, name() + "_player_top_name");
    }

    public int getFromDay() {
        return 0;
    }

    public long getDurationUntilEnd() {
        return -1;
    }



}