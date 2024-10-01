package net.skykingdoms.api.protocol.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * @author Unidentified Person
 */
@UtilityClass
public final class ParseUtils {

    public long parseLong(final @NonNull CharSequence sequence) {
        return parseLong(sequence, 0, sequence.length());
    }

    public long parseLong(final @NonNull CharSequence sequence,
                          int start,
                          final int end) {
        var negate = false;
        var result = 0L;

        switch (sequence.charAt(start)) {
            case '-':
                negate = true;
            case '+':
                start++;
                break;
        }

        for (var i = start; i < end; i++) {
            val c = sequence.charAt(i);

            if (c < '0' || c > '9') {
                continue;
            }

            result = result * 10 + (c & 0xF);
        }

        return negate ? -result : result;
    }

    public int parseInt(final @NonNull CharSequence sequence) {
        return parseInt(sequence, 0, sequence.length());
    }

    public int parseInt(final @NonNull CharSequence sequence,
                        int start,
                        final int end) {
        var negate = false;
        var result = 0;

        switch (sequence.charAt(start)) {
            case '-':
                negate = true;
            case '+':
                start++;
                break;
        }

        for (var i = start; i < end; i++) {
            val c = sequence.charAt(i);

            if (c < '0' || c > '9') {
                continue;
            }

            result = result * 10 + (c & 0xF);
        }

        return negate ? -result : result;
    }

    public double parseDouble(final @NonNull CharSequence sequence) {
        return parseDouble(sequence, 0, sequence.length());
    }

    public double parseDouble(final @NonNull CharSequence sequence,
                              int start,
                              final int end) {
        var negate = false;

        var x = 0;
        var y = 0;
        var yDivision = 1;

        var dot = false;

        switch (sequence.charAt(start)) {
            case '-':
                negate = true;
            case '+':
                start++;
                break;
        }

        for (var i = start; i < end; i++) {
            val c = sequence.charAt(i);

            if (c == '.') {
                dot = true;
                continue;
            }

            if (c < '0' || c > '9') {
                continue;
            }

            val v = c & 0xF;

            if (dot) {
                y = y * 10 + v;
                yDivision *= 10;
            } else {
                x = x * 10 + v;
            }
        }

        val result = x + y / (double) yDivision;

        return negate ? -result : result;
    }

    public static float parseFloat(final @NonNull CharSequence sequence) {
        return parseFloat(sequence, 0, sequence.length());
    }

    public static float parseFloat(final @NonNull CharSequence sequence,
                                   int start,
                                   final int end) {
        var negate = false;

        var x = 0;
        var y = 0;
        var yDivision = 1;

        var dot = false;

        switch (sequence.charAt(start)) {
            case '-':
                negate = true;
            case '+':
                start++;
                break;
        }

        for (var i = start; i < end; i++) {
            val c = sequence.charAt(i);

            if (c == '.') {
                dot = true;
                continue;
            }

            if (c < '0' || c > '9') {
                continue;
            }

            val v = c & 0xF;

            if (dot) {
                y = y * 10 + v;
                yDivision *= 10;
            } else {
                x = x * 10 + v;
            }
        }

        float result = x + y / (float) yDivision;

        return negate ? -result : result;
    }

}
