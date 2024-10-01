package net.skykingdoms.backend.base.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.backend.command.Async;
import net.skykingdoms.api.backend.command.CommandExecutor;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.condition.WithMultipleConditions;
import net.skykingdoms.api.backend.command.cooldown.WithCooldown;
import net.skykingdoms.api.backend.command.cooldown.WrappedCooldown;
import net.skykingdoms.api.backend.command.execution.help.WithoutHelp;
import net.skykingdoms.api.backend.command.localization.WithLocalizationKey;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.base.command.condition.WrappedCompiledCommandCondition;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.tabcomplete.TabcompleteManager;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcompleter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandParams {

    int methodHash;
    CommandWrapper wrapper;
    ExecutorService threadExecutor;

    List<WrappedCompiledCommandCondition> conditions;
    CommandExecutor<ExecutionSession> executor;

    WrappedCooldown cooldown;
    Tabcompleter[] tabcompleters;

    boolean withoutHelp;
    String localizationKey;

    public CommandParams(CommandWrapper wrapper, int hash, CommandExecutor<ExecutionSession> executor) {
        this(
                wrapper, null, hash, Collections.emptyList(),
                executor, WrappedCooldown.EMPTY, null,
                true, null
        );
    }

    public CommandParams(CommandWrapper wrapper, ExecutorService threadExecutor, int methodHash,
                         List<WrappedCompiledCommandCondition> conditions, CommandExecutor<ExecutionSession> executor,
                         WrappedCooldown cooldown, Tabcompleter[] tabcompleters, boolean withoutHelp,
                         String localizationKey) {
        this.wrapper = wrapper;
        this.methodHash = methodHash;
        this.threadExecutor = threadExecutor;
        this.conditions = conditions;
        this.executor = executor;
        this.cooldown = cooldown;
        this.tabcompleters = tabcompleters;
        this.withoutHelp = withoutHelp;
        this.localizationKey = localizationKey;
    }

    public CommandParams(CommandWrapper wrapper, Method method, CommandExecutor<ExecutionSession> executor) {
        this.wrapper = wrapper;
        this.methodHash = method.getName().hashCode();
        this.threadExecutor = getThreadExecutor(method);
        this.conditions = getConditions(method);
        this.executor = executor;
        this.cooldown = getCooldown(method);
        this.tabcompleters = getTabcompleters(method);
        this.withoutHelp = method.isAnnotationPresent(WithoutHelp.class);
        this.localizationKey = getLocalizationKey(method);
    }

    private Tabcompleter[] getTabcompleters(Method method) {
        if (method.isAnnotationPresent(Tabcomplete.class)) {
            val tabcomplete = method.getDeclaredAnnotation(Tabcomplete.class);

            return TabcompleteManager.IMP.parsePattern(tabcomplete.value());
        }

        return null;
    }

    private WrappedCooldown getCooldown(Method method) {
        if (method.isAnnotationPresent(WithCooldown.class)) {
            val cooldown = method.getDeclaredAnnotation(WithCooldown.class);

            return new WrappedCooldown(cooldown.value(), cooldown.currentServerOnly());
        }

        return WrappedCooldown.EMPTY;
    }

    private String getLocalizationKey(Method method) {
        if (method.isAnnotationPresent(WithLocalizationKey.class)) {
            WithLocalizationKey key = method.getDeclaredAnnotation(WithLocalizationKey.class);

            return key.value();
        }

        return null;
    }

    private ExecutorService getThreadExecutor(Method method) {
        return method.isAnnotationPresent(Async.class)
                ? ForkJoinPool.commonPool()
                : null;
    }

    private List<WrappedCompiledCommandCondition> getConditions(Method method) {
        List<String> names = new ArrayList<>();

        val withMultipleConditions = method.getDeclaredAnnotation(WithMultipleConditions.class);

        if (withMultipleConditions != null) {
            val withConditionArray = withMultipleConditions.value();

            names.addAll(Arrays.stream(withConditionArray).map(WithCondition::value)
                    .collect(Collectors.toList()));
        }

        WithCondition withCondition = method.getDeclaredAnnotation(WithCondition.class);

        if (withCondition != null) {
            names.add(withCondition.value());
        }

        return wrapper.getCommandManager().getConditions(names);
    }


}
