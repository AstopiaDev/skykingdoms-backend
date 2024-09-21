package net.skykingdoms.backend.api.protocol.util;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.LinkedList;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class TextFormat {

    String[] textParts;
    String empty;

    public TextFormat(final @NonNull String text) {
        val textParts = new LinkedList<String>();
        var start = 0;

        for (var i = 0; i < text.length(); i++) {
            var ch = text.charAt(i);

            if (ch == '{' && text.charAt(i + 1) == '}') {
                textParts.add(text.substring(start, i));

                i++;
                start = i + 1;
            }
        }

        textParts.add(text.substring(start));

        // К удивлению, LinkedList
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        this.textParts = textParts.toArray(new String[textParts.size()]);
        this.empty = String.join("", text);
    }

    public String format(final Object @Nullable ... o) {
        if (o == null || o.length == 0) return format();

        val sb = FastStrings.getEmptyBuilder();

        for (var i = 0; i < textParts.length; i++) {
            sb.append(textParts[i]);

            if (i < o.length)
                sb.append(o[i]);
        }

        return sb.toString();
    }

    public String format() {
        return empty;
    }

    public static String formatText(final @NonNull String text,
                                    final Object @NonNull ... o) {
        if (o.length == 0) {
            return text;
        }

        int idx = 0;

        StringBuilder sb = FastStrings.getEmptyBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == '{' && text.charAt(i + 1) == '}') {
                sb.append(idx >= o.length ? "" : o[idx++]);
                i++;

                continue;
            }

            sb.append(ch);
        }

        return sb.toString();
    }

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");

    public static String formatNumber(final int number) {
        return DECIMAL_FORMAT.format(number);
    }

}
