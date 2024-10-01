package net.skykingdoms.backend.base.command.condition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.command.CommandExecutor;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;

/**
 * @author Vihara
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class WrappedCompiledCommandCondition implements Comparable<WrappedCompiledCommandCondition> {

    int priority;
    String name;
    boolean showInHelp;

    CommandExecutor<ExecutionSession> checker;

    public WrappedCompiledCommandCondition(final int priority,
                                           final @NonNull String name,
                                           final boolean showInHelp,
                                           final @NonNull CommandExecutor<ExecutionSession> checker) {
        this.priority = priority;
        this.name = name;
        this.showInHelp = showInHelp;
        this.checker = checker;
    }

    @Override
    public int compareTo(final @NonNull WrappedCompiledCommandCondition o) {
        return Integer.compare(priority, o.priority);
    }
}
