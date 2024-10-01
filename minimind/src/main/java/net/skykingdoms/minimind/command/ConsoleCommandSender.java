package net.skykingdoms.minimind.command;

import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.language.Language;
import org.jetbrains.annotations.NotNull;

public class ConsoleCommandSender implements CommandSender {
    private final String name = "Console";

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public Language getLanguage() {
        return Language.DEFAULT;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public void dispatchCommand(String command) {

    }

    @Override
    public void sendMessage(String message) {
        Logging.IMP.info(message);
    }

    @Override
    public void sendRawMessage(String message) {
        Logging.IMP.info(message);
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public boolean isStaff() {
        return true;
    }
}
