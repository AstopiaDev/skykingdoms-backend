package net.skykingdoms.api.protocol.util;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

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

    public static String formatMcToAnsi(final @NonNull String text) {
        return mcFormat.matcher(text).replaceAll((m) -> {
            String r = m.group(1);

            if (r.length() == 1) {
                return mcToAnsi.get(r);
            }

            return hexToAnsi(r);
        });
    }

    private static final Map<String, String> mcToAnsi;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("0", "\u001B[0;30m");
        map.put("1", "\u001B[0;34m");
        map.put("2", "\u001B[0;32m");
        map.put("3", "\u001B[0;36m");
        map.put("4", "\u001B[0;31m");
        map.put("5", "\u001B[0;35m");
        map.put("6", "\u001B[0;33m");
        map.put("7", "\u001B[0;37m");
        map.put("8", "\u001B[0;90m");
        map.put("9", "\u001B[0;94m");
        map.put("a", "\u001B[0;92m");
        map.put("b", "\u001B[0;96m");
        map.put("c", "\u001B[0;91m");
        map.put("d", "\u001B[0;95m");
        map.put("e", "\u001B[0;93m");
        map.put("f", "\u001B[0;97m");

        map.put("k", "\u001B[8m");
        map.put("l", "\u001B[1m");
        map.put("m", "\u001B[9m");
        map.put("n", "\u001B[4m");
        map.put("o", "\u001B[3m");
        map.put("r", "\u001B[0m");

        mcToAnsi = Collections.unmodifiableMap(map);
    }

    private static final Pattern mcFormat = Pattern.compile("§([0-9a-fk-or]|#[0-9a-fA-F]{6})");

    private static String hexToAnsi(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3));
        int g = Integer.parseInt(hex.substring(3, 5));
        int b = Integer.parseInt(hex.substring(5, 7));

        return String.format("\u001B[38;2;%d;%d;%dm", r, g, b);
    }
}
