package net.skykingdoms.backend.base.util.color;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Simplistic enumeration of all supported color values for chat.
 */
public enum ChatColor {

    /**
     * Represents black.
     */
    BLACK('0', "black", 30),
    /**
     * Represents dark blue.
     */
    DARK_BLUE('1', "dark_blue", 34),
    /**
     * Represents dark green.
     */
    DARK_GREEN('2', "dark_green", 32),
    /**
     * Represents dark blue (aqua).
     */
    DARK_AQUA('3', "dark_aqua", 36),
    /**
     * Represents dark red.
     */
    DARK_RED('4', "dark_red", 31),
    /**
     * Represents dark purple.
     */
    DARK_PURPLE('5', "dark_purple", 35),
    /**
     * Represents gold.
     */
    GOLD('6', "gold", 33),
    /**
     * Represents gray.
     */
    GRAY('7', "gray", 37),
    /**
     * Represents dark gray.
     */
    DARK_GRAY('8', "dark_gray", 90),
    /**
     * Represents blue.
     */
    BLUE('9', "blue", 94),
    /**
     * Represents green.
     */
    GREEN('a', "green", 92),
    /**
     * Represents aqua.
     */
    AQUA('b', "aqua", 96),
    /**
     * Represents red.
     */
    RED('c', "red", 91),
    /**
     * Represents light purple.
     */
    LIGHT_PURPLE('d', "light_purple", 95),
    /**
     * Represents yellow.
     */
    YELLOW('e', "yellow", 93),
    /**
     * Represents white.
     */
    WHITE('f', "white", 97),
    /**
     * Represents magical characters that change around randomly.
     */
    MAGIC('k', "obfuscated", 5),
    /**
     * Makes the text bold.
     */
    BOLD('l', "bold", 1),
    /**
     * Makes a line appear through the text.
     */
    STRIKETHROUGH('m', "strikethrough", 9),
    /**
     * Makes the text appear underlined.
     */
    UNDERLINE('n', "underline", 4),
    /**
     * Makes the text italic.
     */
    ITALIC('o', "italic", 3),
    /**
     * Resets all previous chat colors or formats.
     */
    RESET('r', "reset", 0);

    /**
     * The special character which prefixes all chat colour codes. Use this if
     * you need to dynamically convert colour codes from your custom time.
     */
    public static final char COLOR_CHAR = '\u00A7';
    public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    /**
     * Pattern to remove all colour codes.
     */
    public static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-OR]");
    /**
     * Colour instances keyed by their active character.
     */
    private static final Map<Character, ChatColor> BY_CHAR = new HashMap<>();

    /**
     * The code appended to {@link #COLOR_CHAR} to make usable color.
     */
    private final char code;

    private final String ansiString;

    /**
     * This color's color char prefixed by the {@link #COLOR_CHAR}.
     */
    private final String toString;
    @Getter
    private final String name;

    static {
        for (ChatColor colour : values()) {
            BY_CHAR.put(colour.code, colour);
        }
    }

    ChatColor(char code, String name, int colorId) {
        this.code = code;
        this.name = name;
        this.ansiString = "\u001b[" + colorId + "m";
        this.toString = new String(new char[]{COLOR_CHAR, code});
    }

    @Override
    public String toString() {
        return toString;
    }

    public String ansi() {
        return ansiString;
    }

    /**
     * Strips the given message of all color codes
     *
     * @param input String to strip of color
     * @return A copy of the input string, without any coloring
     */
    public static String stripColor(final String input) {
        if (input == null) {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        return translateAlternateColorCodes(altColorChar, textToTranslate, ALL_CODES);
    }

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate, String codes) {
        char[] b = null;

        for (int i = 0; i < textToTranslate.length() - 1; i++) {
            if (textToTranslate.charAt(i) == altColorChar
                    && codes.indexOf(textToTranslate.charAt(i + 1)) > -1) {

                if (b == null) {
                    b = textToTranslate.toCharArray();
                }

                b[i] = ChatColor.COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return b == null ? textToTranslate : new String(b);
    }

    /**
     * Get the color represented by the specified code.
     *
     * @param code the code to search for
     * @return the mapped color, or null if non exists
     */
    public static ChatColor getByChar(char code) {
        return BY_CHAR.get(code);
    }
}

