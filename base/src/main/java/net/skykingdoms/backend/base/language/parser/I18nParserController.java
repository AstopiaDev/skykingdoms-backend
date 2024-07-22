package net.skykingdoms.backend.base.language.parser;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.language.value.LanguageValue;
import net.skykingdoms.backend.base.util.pair.Pair;
import net.skykingdoms.backend.base.util.time.Duration;
import net.skykingdoms.backend.base.util.time.TimeUtil;
import net.skykingdoms.backend.base.util.word.CommonWordsLoader;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public final class I18nParserController implements I18nParser {

    @Getter
    private static final I18nParserController instance = new I18nParserController();

    private final ThreadLocal<I18nThreadParser> THREAD_PARSER
            = ThreadLocal.withInitial(() -> new I18nThreadParser(this));

    private final Map<String, LanguageValue> values = new ConcurrentHashMap<>() {
        {
            put("lang", (modern, language, args) -> {
                if (args == null || args.length == 0) {
                    return null;
                }

                return language.getMessage(modern, args[0], args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : null);
            });

            put("time", (modern, language, args) -> {
                if (args == null || args.length == 0) {
                    return null;
                }

                return TimeUtil.getDateString(language, Long.parseLong(args[0]));
            });

            put("duration_sec", (modern, language, args) -> {
                if (args == null || args.length == 0) {
                    return null;
                }

                return Duration.durationWordsSec(language, Long.parseLong(args[0]));
            });

            put("word", (modern, language, args) -> {
                if (args == null || args.length == 0) {
                    return null;
                }

                val noun = CommonWordsLoader.getNoun(args[0]);
                val word = noun.get(language);

                if (args.length > 1) {
                    word.withNumber(Long.parseLong(args[1]));
                }

                return word.toString();
            });
        }
    };

    private final Cache<Pair<Language, String>, String> cache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .initialCapacity(100)
            .build();

    @Override
    public void clearCache() {
        cache.invalidateAll();
    }

    @Override
    public String parse(boolean modern, final @NonNull Language language, final @NonNull String text) {
        try {
            String nonFinalText = text;
            if (modern) {
                nonFinalText = text.replace("lang:", "lang:pack_");
            }

            Pair<Language, String> cacheKey = new Pair<>(language, nonFinalText);

            if (!cache.asMap().containsKey(cacheKey)) {
                nonFinalText = text;
            }

            String finalNonFinalText = nonFinalText;
            return cache.get(cacheKey, () -> {
                I18nThreadParser parser = THREAD_PARSER.get();
                return parser.parse(modern, language, finalNonFinalText);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public I18nParser getParent() {
        return null;
    }

    @Override
    public void registerValue(final @NonNull String name,
                              final @NonNull LanguageValue value) {
        this.values.put(name, value);
    }

    @Override
    public LanguageValue getValue(final @NonNull String name) {
        return values.get(name);
    }

}