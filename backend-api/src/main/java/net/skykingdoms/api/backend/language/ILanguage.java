package net.skykingdoms.api.backend.language;

import lombok.NonNull;

public interface ILanguage {
    String getName();
    String getMessage(@NonNull String key);
    String getMessage(@NonNull String key, Object... objects);
    String getMessage(boolean modern, @NonNull String key, Object... objects);
    String getMessage(boolean modern, @NonNull String key, boolean useDefault, Object[] objects);
}
