package net.skykingdoms.backend.base.util;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@UtilityClass
public final class Patterns {

    public final Pattern DIGIT_IP = Pattern.compile("(\\d{1,3})(\\.\\d{1,3}){3}");

    public final Pattern INT_NUMBER_PATTERN = Pattern.compile("^(0|[1-9][0-9]*)$");

    public final Pattern URL = Pattern.compile("(?i)http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*(),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+");

    public final Pattern TIME = Pattern.compile(
            "(?i)" +
                    "(\\d{1,3}(?=ns))?" +
                    "(\\d{1,3}(?=mc))?" +
                    "(\\d{1,3}(?=ms))?" +
                    "(\\d{1,3}(?=s))?" +
                    "(\\d{1,3}(?=m))?" +
                    "(\\d{1,3}(?=h))?" +
                    "(\\d{1,3}(?=d))?" +
                    "(\\d{1,3}(?=w))?"
    );
    public final Pattern INTEGER = Pattern.compile("^(-)?\\d+$");

    public boolean isValidInteger(@NonNull String value) {
        return INT_NUMBER_PATTERN.matcher(value).matches();
    }

    public long parseTime(@NonNull String time, @NonNull TimeUnit unitTo) throws IllegalArgumentException {
        val matcher = TIME.matcher(time);

        TObjectIntMap<TimeUnit> values = new TObjectIntHashMap<>();

        while (matcher.find()) {
            for (int i = 1; i <= 7; i++) {
                String value = matcher.group(i);

                if (value == null || value.isEmpty()) {
                    continue;
                }

                TimeUnit unit = TimeUnit.values()[i - 1];
                int intValue = Integer.parseInt(value);

                values.adjustOrPutValue(unit, intValue, intValue);
                break;
            }
        }

        if (values.isEmpty()) {
            throw new IllegalArgumentException("Illegal Date");
        }

        val total = new AtomicLong();

        values.forEachEntry((timeUnit, value) -> {
            total.addAndGet(unitTo.convert(value, timeUnit));

            return true;
        });

        if (total.get() <= 0) {
            throw new IllegalArgumentException("Illegal Date");
        }

        return total.get();
    }

    public boolean isValidUrl(@NonNull String url) {
        return URL.matcher(url).matches();
    }

    public boolean isInteger(@NonNull String s) {
        return INTEGER.matcher(s).matches();
    }

    public boolean isIPV4(@NonNull String ip) {
        return DIGIT_IP.matcher(ip).matches();
    }
}
