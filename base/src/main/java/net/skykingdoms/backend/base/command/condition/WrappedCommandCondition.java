package net.skykingdoms.backend.base.command.condition;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 * @author Vihara
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class WrappedCommandCondition {

    int priority;
    String name;
    boolean showInHelp;

    ConditionChecker checker;

    public WrappedCommandCondition(final int priority,
                                   final @NonNull String name,
                                   final boolean showInHelp,
                                   final @NonNull ConditionChecker checker) {
        this.priority = priority;
        this.name = name;
        this.showInHelp = showInHelp;
        this.checker = checker;
    }

    public WrappedCompiledCommandCondition compile(String meta) {
        return new WrappedCompiledCommandCondition(priority, name, showInHelp,
                session -> checker.checkCondition(session, meta));
    }

    public String toString() {
        return "WrappedCommandCondition[name=" + name + ", priority=" + priority + "]";
    }

}
