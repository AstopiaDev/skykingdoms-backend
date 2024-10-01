package net.skykingdoms.api.backend.gamer;

import lombok.NonNull;
import net.skykingdoms.api.backend.language.ILanguage;
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

    ILanguage getLanguage();

    boolean isConsole();

}
