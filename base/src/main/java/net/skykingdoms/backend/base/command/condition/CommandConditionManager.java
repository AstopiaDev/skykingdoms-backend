package net.skykingdoms.backend.base.command.condition;

import net.skykingdoms.api.backend.command.CommandExecutor;
import net.skykingdoms.api.backend.command.condition.ConditionProvider;
import net.skykingdoms.api.backend.command.execution.IExecutionSession;
import net.skykingdoms.backend.base.util.metafactory.MetafactoryBuilder;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Vihara
 */
public final class CommandConditionManager {

    private static final Map<String, WrappedCommandCondition> BY_NAME
            = new HashMap<>();

    static {
        CommandConditionManager manager = new CommandConditionManager();
        manager.addCondition(CommonCommandConditions.class);
    }

    public void addCondition(Object o) {
        addCondition(o, o.getClass());
    }

    public void addCondition(Class<?> cls) {
        addCondition(null, cls);
    }

    private void addCondition(Object instance, Class<?> cls) {
        for (Method method : cls.getDeclaredMethods()) {
            ConditionProvider condition = method.getAnnotation(ConditionProvider.class);

            if (condition == null) {
                continue;
            }

            ConditionChecker checker = new MetafactoryBuilder<>(ConditionChecker.class)
                    .target(method).buildLambda(instance);

            WrappedCommandCondition commandCondition = new WrappedCommandCondition(
                    condition.priority(), condition.value(),
                    condition.showInHelp(), checker
            );

            synchronized (BY_NAME) {
                BY_NAME.put(condition.value().toLowerCase(), commandCondition);
            }
        }
    }

    @Deprecated
    public <T extends IExecutionSession> void addCondition(int priority, String name, CommandExecutor<T> sessionChecker) {
        addCondition(priority, name, false, sessionChecker.unchecked());
    }

    @Deprecated
    public <T extends IExecutionSession> void addCondition(int priority, String name, boolean b, CommandExecutor<T> sessionChecker) {
        WrappedCommandCondition commandCondition = new WrappedCommandCondition(priority, name, b, (session, args) -> sessionChecker.execute((T) session));

        synchronized (BY_NAME) {
            BY_NAME.put(name.toLowerCase(), commandCondition);
        }
    }

    public List<WrappedCompiledCommandCondition> getConditions(List<String> conditions) {
        List<WrappedCompiledCommandCondition> result = new ArrayList<>(conditions.size());

        for (String condition : conditions) {
            int separator = condition.indexOf(": ");

            String name = separator == -1 ? condition : condition.substring(0, separator);
            String meta = separator == -1 ? "" : condition.substring(separator + 2);

            WrappedCommandCondition commandCondition = BY_NAME.get(name.toLowerCase());

            if (commandCondition == null) {
                throw new IllegalStateException("Unknown condition: " + name + " (meta: " + meta + ")");
            }

            result.add(commandCondition.compile(meta));
        }

        Collections.sort(result);

        return result;
    }

}
