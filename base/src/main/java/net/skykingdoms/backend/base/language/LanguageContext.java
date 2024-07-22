package net.skykingdoms.backend.base.language;

import lombok.NonNull;
import net.skykingdoms.backend.base.util.string.ObjectLinkedBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * shit required for sending some kind of GLOBAL messages :PPP
 */
public final class LanguageContext {

    private final Function<Language, @NotNull String> messageProvider;

    public LanguageContext(@NonNull String key, @NonNull Function<Language, ObjectLinkedBuf> paramsFactory) {
        this.messageProvider = language -> language.getMessage(false, key, paramsFactory.apply(language).toArray());
    }

    public LanguageContext(final @NonNull Function<Language, String> messageProvider) {
        this.messageProvider = messageProvider;
    }

    public String format(final @NonNull Language language) {
        return messageProvider.apply(language);
    }

}
