package net.skykingdoms.backend.base.language.value;

import net.skykingdoms.backend.base.language.Language;
import org.jetbrains.annotations.NotNull;

public interface LanguageValue {

    String process(boolean modern, final @NotNull Language language, final @NotNull String[] args);

}
