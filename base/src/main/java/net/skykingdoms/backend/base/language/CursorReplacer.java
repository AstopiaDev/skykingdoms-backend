package net.skykingdoms.backend.base.language;

import lombok.NonNull;
import net.skykingdoms.backend.base.util.string.StringUtil;

public enum CursorReplacer {

    LOWER {
        @Override
        protected String replace(final @NonNull String value) {
            return value.toLowerCase();
        }
    },
    UPPER {
        @Override
        protected String replace(final @NonNull String value) {
            return value.toUpperCase();
        }
    },
    FIRST_UP {
        @Override
        protected String replace(final @NonNull String value) {
            return value.length() > 1 ? StringUtil.capitalize(value) : UPPER.replace(value);
        }
    },
    WORD_UP {
        @Override
        protected String replace(@NonNull String value) {
            String[] words = value.split(" ");

            for (String word : words) {
                value = value.replaceFirst(word, FIRST_UP.replace(word));
            }

            return value;
        }
    };

    protected abstract String replace(final @NonNull String value);

    // ---------------------------------------------------------------

    public static CursorReplacer get(final @NonNull String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
