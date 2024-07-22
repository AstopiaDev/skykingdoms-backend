package net.skykingdoms.backend.base.gamer;

import lombok.NonNull;
import net.skykingdoms.backend.base.language.Language;
import org.jetbrains.annotations.NotNull;

public interface ICommandSender {

    @NotNull String getName();

    void sendMessage(final String message);
    void sendRawMessage(final String message);

    String getDisplayName();

    default void sendLocalizedMessage(boolean modern, @NonNull String key, Object... objects) {
        sendMessage(getLanguage().getMessage(!isConsole() && modern, key, objects));
    }

    void dispatchCommand(String command);

    Language getLanguage();

    boolean isConsole();

}
