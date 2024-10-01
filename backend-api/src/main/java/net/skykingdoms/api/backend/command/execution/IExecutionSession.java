package net.skykingdoms.api.backend.command.execution;

import net.skykingdoms.api.backend.command.exception.CommandException;
import net.skykingdoms.api.backend.gamer.ICommandSender;

import javax.annotation.Nullable;

public interface IExecutionSession {
    void sendMessage(String message);
    void sendMessage(@Nullable String message, @Nullable Object... o);
    String[] getArguments(int offset);
    String getArgumentString();
    String getArgumentString(int offset);
    int getArgumentCount();
    String getArgument(int i);
    int getIntArgument(int i, String field) throws CommandException;
    ICommandSender getSender();
}
