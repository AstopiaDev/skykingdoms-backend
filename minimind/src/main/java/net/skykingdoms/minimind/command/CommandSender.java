package net.skykingdoms.minimind.command;

import lombok.NonNull;
import net.skykingdoms.api.backend.gamer.ICommandSender;

public interface CommandSender extends ICommandSender {
    default void sendCommandUsageMessage(@NonNull String usageKey) {
        sendLocalizedMessage(false, "command_usage", "%usage%", getLanguage().getMessage(false, usageKey));
    }

    boolean isStaff();
}
