package net.skykingdoms.backend.base.util.time;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.skykingdoms.backend.base.language.Language;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class TimeUtil {

    public long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public long currentTimeMinutes() {
        return System.currentTimeMillis() / 1000 / 60;
    }

    public long getDayUTC() {
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis());
    }

    public int getNow() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public String getTodayString(@NonNull Language language) {
        return getDateString(language, LocalDate.now());
    }

    public String getDateString(@NonNull Language language, long unixTime) {
        LocalDate localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTime), ZoneId.systemDefault()).toLocalDate();

        return getDateString(language, localDate);
    }

    public String getDateString(@NonNull Language language, @NonNull LocalDate localDate) {
        return String.format("%d %s %d",
                localDate.getDayOfMonth(),
                localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag(language.getShortName())),
                localDate.getYear());
    }

    public String getDateTimeString(@NonNull Language language, @NonNull LocalDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM uuuu HH:mm:ss",
                Locale.forLanguageTag(language.getShortName()));

        return formatter.format(time);
    }

    public String getDateTimeStringEu(@NonNull LocalDateTime time) {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm").format(Timestamp.valueOf(time));
    }

    public String getTimeString(int time) {
        return getTimeString(time, 120, 300);
    }

    public String getTimeString(int time, int red, int orange) {
        String color = "§a";
        if (time <= red) {
            color = "§c";
        } else if (time <= orange) {
            color = "§6";
        }

        return color + Duration.simple(time);
    }

    public String getEnding(long amount, String single, String onemore, String plural) {
        amount %= 100;

        if (amount < 11 || amount > 14) {
            switch ((int) (amount % 10)) {
                case 1:
                    return single;
                case 2:
                case 3:
                case 4:
                    return onemore;
            }
        }

        return plural;
    }

    public String getTime(int seconds) {
        int minutes = 0;
        int hours = 0;
        int days = 0;
        int weeks = 0;
        int months = 0;
        int years = 0;

        if (seconds >= 60) {
            final int i = seconds / 60;
            seconds -= 60 * i;
            minutes += i;
        }

        if (minutes >= 60) {
            final int i = minutes / 60;
            minutes -= 60 * i;
            hours += i;
        }

        if (hours >= 24) {
            int i = hours / 24;
            hours -= 24 * i;
            days += i;
        }

        if (days >= 7) {
            int i = days / 7;
            days -= 7 * i;
            weeks += i;
        }

        if (weeks >= 4) {
            int i = weeks / 4;
            weeks -= 4 * i;
            months += i;
        }

        if (months >= 12) {
            final int i = months / 12;
            months -= 12 * i;
            years += i;
        }

        StringBuilder builder = new StringBuilder();

        if (years != 0) {
            builder.append(getEnding(years, "год", "года", "лет")).append(" ");
        }

        if (months != 0) {
            builder.append(getEnding(months, "месяц", "месяца", "месяцев")).append(" ");
        }

        if (weeks != 0) {
            builder.append(getEnding(weeks, "неделя", "недели", "недель")).append(" ");
        }

        if (days != 0) {
            builder.append(getEnding(days, "день", "дня", "дней")).append(" ");
        }

        if (hours != 0) {
            builder.append(getEnding(hours, "час", "часа", "часов")).append(" ");
        }

        if (minutes != 0) {
            builder.append(getEnding(minutes, "минута", "минуты", "минут")).append(" ");
        }

        if (seconds != 0) {
            builder.append(getEnding(seconds, "секунда", "секунды", "секунд"));
        }

        return builder.toString();
    }

}
