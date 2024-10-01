package net.skykingdoms.backend.base.language;

import lombok.NonNull;
import net.skykingdoms.api.backend.language.ILanguage;
import net.skykingdoms.api.backend.language.ILanguageContext;
import net.skykingdoms.backend.base.util.string.ObjectLinkedBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * shit required for sending some kind of GLOBAL messages :PPP
 */
public final class LanguageContext implements ILanguageContext {

    private final Function<Language, @NotNull String> messageProvider;

    public LanguageContext(@NonNull String key, Function<Language, ObjectLinkedBuf> paramsFactory) {
        this.messageProvider = language -> language.getMessage(false, key, paramsFactory.apply(language).toArray());
    }

    public LanguageContext(final @NonNull Function<Language, String> messageProvider) {
        this.messageProvider = messageProvider;
    }

    @Override
    public <T extends ILanguage> String format(final @NonNull T language) {
        return messageProvider.apply((Language) language);
    }
}
