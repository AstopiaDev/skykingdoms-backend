package net.skykingdoms.backend.base.command;

import net.skykingdoms.backend.base.command.execution.ExecutionSession;

public interface CommandExecutor<Session extends ExecutionSession> {

    void execute(Session session) throws Exception;

    @SuppressWarnings("unchecked")
    default CommandExecutor<ExecutionSession> unchecked() {
        return session -> execute((Session) session);
    }

}
