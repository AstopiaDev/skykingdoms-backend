package net.skykingdoms.backend.base.command;

import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;

public abstract class DynamicCommand extends CommandWrapper {
    public DynamicCommand(CommandManager manager, String name, String... aliases) {
        super(manager, name, aliases);
        CommandParams params = new CommandParams(this, 0, this::execute);
        WrappedCommandExecutor wrappedCommandExecutor = new WrappedCommandExecutor(params);
        setExecutor(wrappedCommandExecutor);
    }

    protected abstract <T extends ExecutionSession> void execute(T session);
}
