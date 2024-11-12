package net.skykingdoms.backend.megamind.api.command;

import lombok.NonNull;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import net.skykingdoms.api.backend.permission.UserRankContainer;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.megamind.MegamindApp;


public interface CommandSender extends ICommandSender {

    @Override
    default void dispatchCommand(final @NonNull String command) {
        MegamindApp.INSTANCE.executeCommand(this, command);
    }

    default void sendCommandUsageMessage(@NonNull String usageKey) {
        sendLocalizedMessage(false, "command_usage", "%usage%", getLanguage().getMessage(false, usageKey));
    }

    UserRankContainer<UserRank> getRanks();

    boolean isStaff();

}
