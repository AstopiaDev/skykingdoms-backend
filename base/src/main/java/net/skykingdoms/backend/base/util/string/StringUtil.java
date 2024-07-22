package net.skykingdoms.backend.base.util.string;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class StringUtil {

    // remove non-printable and non-cyrillic chars
    public String retainAsciiAndCyrillic(@NonNull String str) {
        // ' -}' means all chars from space to '}'
        return str.replaceAll("[^ -}а-яА-ЯёЁ]|['`]", "").trim();
    }

    public boolean startsWith(String haystack, String needle, boolean ignoreCase) {
        return haystack.regionMatches(ignoreCase, 0, needle, 0, needle.length());
    }

    public String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public String[] getWords(@NonNull String str) {
        List<String> words = new ArrayList<>(Arrays.asList(str.trim().toLowerCase().split("[^а-яёa-z]")));

        words.removeIf(s -> s.length() < 2);

        return words.toArray(new String[0]);
    }

    public static boolean hasEqualStart(String line, String str, int n) {
        if (line.equals(str)) {
            return true;
        }

        char[] wordCharacters = line.toCharArray();
        char[] targetCharacters = str.toCharArray();

        // если длина меньше чем нам надо совпадающих символов - обрезаем лимит
        if (wordCharacters.length < n) {
            n = wordCharacters.length;
        }

        int similarChars = 0;

        for (int i = 0; i < wordCharacters.length; i++) {
            if (i >= targetCharacters.length || targetCharacters[i] != wordCharacters[i]) {
                break;
            }

            similarChars++;
        }

        return similarChars >= n;
    }

    public boolean isWordSimilar(@NonNull String word, @NonNull String str) {
        if (word.equals(str)) {
            return true;
        }

        return LevenshteinDistance.compute(word, str) < Math.max(word.length(), str.length()) * 0.33;
    }

    public boolean isSimilar(@NonNull String str, @NonNull String[] words) {
        String[] w = getWords(str);

        int similar = 0;
        for (String word : words) {
            for (String otherWord : w) {
                if (isWordSimilar(word, otherWord)) {
                    similar++;
                    break;
                }
            }
        }

        return similar >= (w.length + words.length) * 0.5 * 0.6;
    }

    public static String concat(@NonNull String[] parts, @NonNull String delim) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            builder.append(parts[i]);

            if (i < parts.length - 1) {
                builder.append(delim);
            }
        }

        return builder.toString();
    }

    public static int getSimilarCount(@NonNull String[] w, @NonNull String[] words) {
        int similar = 0;
        for (String word : words) {
            for (String otherWord : w) {
                if (isWordSimilar(word, otherWord)) {
                    similar++;
                    break;
                }
            }
        }

        return similar;
    }

    // todo: rewrite
    public static int[] deserializeIntArray(final @NonNull String input) {
        var replaced = input.replace("[", "");
        replaced = replaced.replace("]", "");

        if (replaced.isEmpty() || replaced.isBlank()) return new int[0];

        return Arrays.stream(replaced.split(", ")).mapToInt(Integer::parseInt).toArray();
    }

    public static String getLastWord(final @NonNull String input) {
        return input.substring(input.lastIndexOf(" ") + 1);
    }


}
