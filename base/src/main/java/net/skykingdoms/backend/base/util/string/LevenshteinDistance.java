package net.skykingdoms.backend.base.util.string;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * pasted and detected (nigger stealer momento)
 */
@UtilityClass
public final class LevenshteinDistance {

    private int min(final int a, final int b, final int c) {
        return Math.min(Math.min(a, b), c);
    }

    // https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
    public int compute(@NonNull CharSequence lhs, @NonNull CharSequence rhs) {
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

        for (var i = 0; i <= lhs.length(); i++) {
            distance[i][0] = i;
        }

        for (var j = 1; j <= rhs.length(); j++) {
            distance[0][j] = j;
        }

        for (var i = 1; i <= lhs.length(); i++) {
            for (var j = 1; j <= rhs.length(); j++) {
                distance[i][j] = min(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1)
                );
            }
        }

        return distance[lhs.length()][rhs.length()];
    }

}
