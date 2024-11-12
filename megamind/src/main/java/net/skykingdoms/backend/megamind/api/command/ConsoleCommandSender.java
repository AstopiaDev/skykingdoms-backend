package net.skykingdoms.backend.megamind.api.command;

import net.skykingdoms.api.backend.permission.UserRankContainer;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.permission.UserRank;
import org.jetbrains.annotations.NotNull;

public enum ConsoleCommandSender implements CommandSender {
    INSTANCE;

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
    public UserRankContainer<UserRank> getRanks() {
        return null;
    }

    @Override
    public boolean isStaff() {
        return true;
    }

}
