package net.skykingdoms.backend.base.language;

import lombok.*;
import net.skykingdoms.api.backend.language.ILanguage;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.language.loaders.GithubLanguageLoader;
import net.skykingdoms.backend.base.language.loaders.LanguageLoader;
import net.skykingdoms.backend.base.util.time.TimeUtil;
import net.skykingdoms.backend.protocol.locale.UserLocale;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Language implements UserLocale, ILanguage {
    RUSSIAN("Русский", "ru", 'а', 'я'),
    ENGLISH("English", "en", 'a', 'z'),
    SLOVENIAN("Slovenian", "slo", 'a', 'z');

    @Getter
    private final int mask = 1 << ordinal();

    public static final Language[] VALUES = values();

    public static final Language DEFAULT = ENGLISH;

    private static final String TRANSLATION_NOT_FOUND_STRING = "§cNo translation";

    private static final String TRANSLATION_ILLEGAL_PARAMETERS = "§cIllegal params passed";

    private static final Pattern CURSOR_PATTERN = Pattern.compile("\\$\\w+(?i)(:LOWER|:UPPER|:FIRST_UP|:WORD_UP)?\\$");

    private static final Yaml YAML = newSnakeYaml();

    public static final LanguageLoader DEFAULT_LOADER = new GithubLanguageLoader();

    private final String languageName;

    private final String shortName;

    private final char alphabetStart, alphabetEnd;
    private final char upperAlphabetStart, upperAlphabetEnd;

    @Setter
    private Map<String, String> messageTranslations;

    @Getter
    private final float translationPercentage = 100;

    Language(String languageName, String shortName, char alphabetStart, char alphabetEnd) {
        this.languageName = languageName;
        this.shortName = shortName;
        this.alphabetStart = alphabetStart;
        this.upperAlphabetStart = Character.toUpperCase(alphabetStart);

        this.alphabetEnd = alphabetEnd;
        this.upperAlphabetEnd = Character.toUpperCase(alphabetEnd);
    }


    public boolean isInAlphabet(char value) {
        return (value >= alphabetStart && value <= alphabetEnd)
                || (value >= upperAlphabetStart && value <= upperAlphabetEnd);
    }

    private static String format(@NonNull String string, Object... objects) {
        if (objects == null) {
            return string;
        }

        if (objects.length % 2 != 0) {
            Logging.IMP.warn(string + "[" + objects.length + "]: " + Arrays.toString(objects));

            return string;
        }

        for (int i = 0; i < objects.length; i = i + 2) {
            Object key = objects[i];

            if (key == null) {
                throw new IllegalStateException("Key can't be null");
            }

            Object value = objects[i + 1];

            string = string.replace(key.toString(), value == null ? "null" : value.toString());
        }

        return string;
    }

    @Override
    public String getName() {
        return name();
    }

    public int getId() {
        return ordinal() + 1;
    }

    @Override
    public String getMessage(@NonNull String key) {
        return getMessage(false, key, true, null);
    }

    @Override
    public String getMessage(@NonNull String key, Object... objects) {
        return getMessage(false, key, true, objects);
    }

    @Override
    public String getMessage(boolean modern, @NonNull String key, Object... objects) {
        return getMessage(modern, key, true, objects);
    }

    @Override
    public String getMessage(boolean modern, @NonNull String key, boolean useDefault, Object[] objects) {
        String message = messageTranslations.get(key.toLowerCase());
        if (modern) {
            message = messageTranslations.get("pack_" + key.toLowerCase());
        }

        if (message == null) {
            message = messageTranslations.get(key.toLowerCase());
        }

        if (message == null) {
            if (useDefault) {
                message = DEFAULT.getMessage(modern, key, false, objects);
            }
        }

        return message == null ? key : format(message, objects);
    }

    public List<String> getMessageList(@NonNull String key, Object... objects) {
        return getMessageList(false, key, true, objects);
    }

    public List<String> getMessageList(boolean modern, @NonNull String key, Object... objects) {
        return getMessageList(modern, key, true, objects);
    }

    public List<String> getMessageList(boolean modern, @NonNull String key, boolean useDefault, Object[] objects) {
        return Arrays.asList(getMessage(modern, key, useDefault, objects).split("\n"));
    }

    public boolean isMessageTranslated(@NonNull String key) {
        boolean translated = messageTranslations.containsKey(key.toLowerCase());

        // recursion fix $$$
        if (this == DEFAULT) {
            return translated;
        }

        return translated || DEFAULT.isMessageTranslated(key);
    }

    // ------------------------------------------------------

    public String getEnding(boolean modern, final int amount,
                            final @NonNull String key) {
        return TimeUtil.getEnding(amount, getMessage(modern, key + "_single"), getMessage(modern, key + "_onemore"), getMessage(modern, key + "_plural"));
    }

    public String getEnding(final int amount,
                            final @NonNull String key) {
        return TimeUtil.getEnding(amount, getMessage(false, key + "_single"), getMessage(false, key + "_onemore"), getMessage(false, key + "_plural"));
    }


    public void reload(final Map<String, String> defaultTranslations, final List<String> entries) {
        if (entries.isEmpty()) {
            messageTranslations = Collections.emptyMap();
           // Logging.IMP.info("EMPTY");
            return;
        }

        final Map<String, String> finalMap = entries.stream()
                .map(text -> {
                    synchronized (YAML) {
                        val result = YAML.<Map<?, ?>>load(text);

                        if (result == null) {
                            System.out.println(text);
                        }

                        return result;
                    }
                })
                .filter(Objects::nonNull)
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString().toLowerCase(),
                        entry -> Objects.toString(entry.getValue()),
                        (a, b) -> {
                      //      System.out.println("Duplicate found: " + a);
                            return a;
                        }
                ));

//        final Map<String, String> finalMap = rawMessageMap.entrySet().stream()
//                .collect(HashMap::new, (m, e) -> m.put(e.getKey().toLowerCase(), e.getValue()), Map::putAll);

        finalMap.replaceAll((key, value) -> processCursors(value, defaultTranslations, finalMap));

        this.messageTranslations = finalMap;
        //Logging.IMP.info(this.messageTranslations.toString());
    }

    public void setTranslation(String key, String value) {
        messageTranslations.put(key, value);
    }

    public String formatTranslationPercentage() {
        return String.format("%.2f", getTranslationPercentage());
    }

    private String processCursors(@NonNull String value,
                                  Map<String, String> defaultMessages,
                                  Map<String, String> messages) {
        Matcher matcher = CURSOR_PATTERN.matcher(value);

        while (matcher.find()) {
            String cursor = matcher.group();
            cursor = cursor.substring(1, cursor.length() - 1);

            String localizationKey;
            CursorReplacer replacer;

            int separator = cursor.indexOf(':');

            if (separator == -1) {
                localizationKey = cursor;
                replacer = null;
            } else {
                replacer = CursorReplacer.get(cursor.substring(separator + 1));
                localizationKey = cursor.substring(0, separator);
            }

            try {
                var result = Optional.of(messages.get(localizationKey))
                        .orElseGet(() -> defaultMessages.get(localizationKey));

                if (replacer != null) {
                    result = replacer.replace(result);
                }

                matcher = CURSOR_PATTERN.matcher(value = matcher.replaceFirst(result));
            } catch (Exception e) {
                Logging.IMP.error("Localization loading fucked up on: {}", localizationKey);
            }
        }

        return value;
    }

    @Deprecated
    public static void addFlag(String flag) {
    }

    @SneakyThrows
    public static void reloadLanguages() {
        DEFAULT_LOADER.loadLanguages();
    }

    @SneakyThrows
    public static void reloadLanguages(final @Nullable LanguageLoader loader) {
        if (loader == null) {
            reloadLanguages(); // using default loader
            return;
        }

        loader.loadLanguages();
    }

    public static Language getById(int id) {
        return id >= 1 && id <= VALUES.length ? VALUES[id - 1] : DEFAULT;
    }

    public static Language getByName(String name) {
        for (Language language : VALUES) {
            if (language.shortName.equalsIgnoreCase(name)) {
                return language;
            }
        }

        return DEFAULT;
    }

    private static Yaml newSnakeYaml() {
        Constructor constructor = new Constructor(new LoaderOptions());

        PropertyUtils propertyUtils = new PropertyUtils();
        propertyUtils.setSkipMissingProperties(true);

        constructor.setPropertyUtils(propertyUtils);

        return new Yaml(constructor);
    }

}
