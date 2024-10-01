package net.skykingdoms.api.backend.language;

import lombok.NonNull;

public interface ILanguageContext {
    <T extends ILanguage> String format(final @NonNull T language);
}
