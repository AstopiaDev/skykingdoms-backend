package net.skykingdoms.backend.base.command.condition;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.skykingdoms.api.backend.command.CommandExecutor;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;

/**
 * @author Vihara
 */
@UtilityClass
@Deprecated // ebal blya :(
public final class CommandConditionProvider {

    private static final CommandConditionManager MANAGER = new CommandConditionManager();

    @Deprecated
    public <T extends ExecutionSession> void addCondition(final int priority,
                                                          final @NonNull String name,
                                                          final @NonNull CommandExecutor<T> sessionChecker) {
        MANAGER.addCondition(priority, name, sessionChecker);
    }

    @Deprecated
    public <T extends ExecutionSession> void addCondition(int priority, String name, boolean b, CommandExecutor<T> sessionChecker) {
        MANAGER.addCondition(priority, name, b, sessionChecker);
    }

}
