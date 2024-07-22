package net.skykingdoms.backend.base.command.condition;

import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import org.jetbrains.annotations.NotNull;

public interface ConditionChecker {

    void checkCondition(final @NotNull ExecutionSession session,
                        final @NotNull String meta) throws Exception;

}
