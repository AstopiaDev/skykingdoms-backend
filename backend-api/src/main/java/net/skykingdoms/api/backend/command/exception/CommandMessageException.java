package net.skykingdoms.api.backend.command.exception;

import net.skykingdoms.api.backend.command.execution.IExecutionSession;

import java.util.List;

public class CommandMessageException extends CommandException {

    private final String[] messages;

    public CommandMessageException(String... messages) {
        this.messages = messages;
    }

    public CommandMessageException(List<String> messages) {
        this.messages = messages.toArray(new String[0]);
    }

    public <T extends IExecutionSession> void printMessages(T session) {
        for (String message : messages) {
            session.sendMessage(message);
        }
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        return this;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
