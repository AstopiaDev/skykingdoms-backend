package net.skykingdoms.backend.base.language.parser;


import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.language.value.LanguageValue;
import org.jetbrains.annotations.NotNull;

public interface I18nParser {

    I18nParser getParent();

    void registerValue(final @NotNull String name, final @NotNull LanguageValue value);
    LanguageValue getValue(final @NotNull String name);

    void clearCache();

    String parse(boolean modern, final @NotNull Language language, final @NotNull String text);

}
