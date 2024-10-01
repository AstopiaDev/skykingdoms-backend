package net.skykingdoms.backend.base.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.command.*;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.execution.FallbackExecutor;
import net.skykingdoms.api.backend.command.execution.PrepareExecution;
import net.skykingdoms.api.backend.command.execution.SubcommandExecutor;
import net.skykingdoms.api.backend.command.execution.help.WithHelp;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import net.skykingdoms.api.backend.gamer.IGamer;
import net.skykingdoms.backend.base.command.condition.CommandConditionManager;
import net.skykingdoms.backend.base.command.condition.WrappedCompiledCommandCondition;
import net.skykingdoms.backend.base.command.condition.WrappedSubcommandExecutor;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;
import net.skykingdoms.backend.base.command.execution.help.HelpCommandExecutor;
import net.skykingdoms.backend.base.command.execution.help.HelpSubcommandExecutor;
import net.skykingdoms.backend.base.dependency.DependencyInjection;
import net.skykingdoms.backend.base.util.metafactory.MetafactoryBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CommandManager {

    private final CommandConditionManager commandConditionManager;

    public CommandManager() {
        this.commandConditionManager = new CommandConditionManager();
    }

    public abstract void registerCommand(String name, ICommand command);

    public void addCondition(Object o) {
        commandConditionManager.addCondition(o);
    }

    public void addCondition(Class<?> cls) {
        commandConditionManager.addCondition(cls);
    }

    public List<WrappedCompiledCommandCondition> getConditions(List<String> conditions) {
        return commandConditionManager.getConditions(conditions);
    }

    public void addCommand(@NotNull Class<?> cls) {
        addCommand(cls, null);
    }

    public void addCommand(@NotNull Object instance) {
        addCommand(instance.getClass(), instance);
    }

    public abstract String getServerName();

    public abstract ExecutionSession newCommandSession(
            final @NonNull WrappedCommandExecutor executor,
            final @NonNull ICommandSender sender,
            final @NonNull IGamer gamer,
            final @NonNull String[] args
    );

    private void addCommand(@NotNull Class<?> cls, @Nullable Object instance) {
        if (!cls.isAnnotationPresent(NamedCommand.class)) {
            throw new IllegalStateException("Command must be annotated with @NamedCommand");
        }

        DependencyInjection.injectDependencies(cls, instance);

        NamedCommand command = cls.getDeclaredAnnotation(NamedCommand.class);
        String commandName = command.value();
        String[] commandAliases = getAliases(cls);

        CommandWrapper wrapper = new CommandWrapper(this, commandName, commandAliases);

        if (cls.isAnnotationPresent(WithHelp.class)) {
            WithHelp help = cls.getDeclaredAnnotation(WithHelp.class);

            boolean primary = help.primary();

            if (primary) {
                wrapper.setExecutor(new HelpCommandExecutor(wrapper));
            }

            wrapper.addSubcommand(new HelpSubcommandExecutor(wrapper));
        }

        CommandExecutor<ExecutionSession> prepareSession = Arrays.stream(cls.getMethods()).filter(method -> {
            return method.isAnnotationPresent(PrepareExecution.class);
        }).map(method -> createExecutor(instance, method)).findFirst().orElse(null);

        for (Method method : cls.getMethods()) {
            try {
                // shitcode, but good for the first time
                if (method.isAnnotationPresent(RelayExecutor.class)) {
                    val name = method.getDeclaredAnnotation(RelayExecutor.class).value();

                    // getting RelayExecutorAliases
                    String[] aliases = {};
                    if (method.isAnnotationPresent(RelayExecutorAlias.class)) {
                        aliases = method.getDeclaredAnnotation(RelayExecutorAlias.class).value();
                    }

                    val _wrapper = new CommandWrapper(this, name, aliases);
                    _wrapper.setPrepareSession(prepareSession);

                    // creating command executor for this method
                    _wrapper.setExecutor(createCommandExecutor(_wrapper, instance, method));

                    // registering primary command
                    registerCommand(name, _wrapper);

                    // registering aliases
                    for (val alias : _wrapper.getAliases()) {
                        registerCommand(alias, _wrapper);
                    }
                }

                for (CommandType type : CommandType.values()) {
                    if (method.isAnnotationPresent(type.cls)) {
                        type.apply(wrapper, instance, method);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error occurred until adding a command: [Method: " + method.getName() + "; Class: " + cls + "]");

                e.printStackTrace();
            }
        }

        wrapper.setPrepareSession(prepareSession);

        registerCommand(commandName, wrapper);

        for (String alias : wrapper.getAliases()) {
            registerCommand(alias, wrapper);
        }
    }

    private static WrappedCommandExecutor createCommandExecutor(CommandWrapper wrapper, Object instance,
                                                                Method method) {
        return new WrappedCommandExecutor(
                new CommandParams(wrapper, method, createExecutor(instance, method))
        );
    }

    private static WrappedSubcommandExecutor createSubcommandExecutor(CommandWrapper wrapper, Object instance,
                                                                      Method method) {
        SubcommandExecutor subcommandExecutor = method.getDeclaredAnnotation(SubcommandExecutor.class);

        String[] aliases = getAliases(method);
        String[] names = new String[1 + aliases.length];
        names[0] = subcommandExecutor.value();

        System.arraycopy(aliases, 0, names, 1, aliases.length);

        return new WrappedSubcommandExecutor(
                names, new CommandParams(wrapper, method, createExecutor(instance, method))
        );
    }

    private static String[] getAliases(AnnotatedElement element) {
        List<String> totalAliases = new ArrayList<>();

        WithAlias withAlias = element.getDeclaredAnnotation(WithAlias.class);

        if (withAlias != null) {
            totalAliases.add(withAlias.value());
        }

        WithMultipleAliases withMultipleAliases = element.getDeclaredAnnotation(WithMultipleAliases.class);

        if (withMultipleAliases != null) {
            totalAliases.addAll(Arrays.stream(withMultipleAliases.value()).map(WithAlias::value)
                    .collect(Collectors.toList()));
        }

        return totalAliases.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    private static CommandExecutor<ExecutionSession> createExecutor(@Nullable Object object, @NotNull Method method) {
        boolean isStatic = Modifier.isStatic(method.getModifiers());

        if (!isStatic && object == null) {
            throw new IllegalStateException("Can't get invoker for non-static method without instance");
        }

        return isStatic
                ? new MetafactoryBuilder<>(CommandExecutor.class).target(method).buildLambda()
                : new MetafactoryBuilder<>(CommandExecutor.class).target(method).buildLambda(object);
    }

    public enum CommandType {

        MAIN(Executor.class) {
            @Override
            public void apply(CommandWrapper wrapper, Object instance, Method method) {
                wrapper.setExecutor(createCommandExecutor(wrapper, instance, method));
            }
        },
        SUB(SubcommandExecutor.class) {
            @Override
            public void apply(CommandWrapper wrapper, Object instance, Method method) {
                wrapper.addSubcommand(createSubcommandExecutor(wrapper, instance, method));
            }
        },
        FALLBACK(FallbackExecutor.class) {
            @Override
            public void apply(CommandWrapper wrapper, Object instance, Method method) {
                wrapper.setFallback(createCommandExecutor(wrapper, instance, method));
            }
        };

        private final Class<? extends Annotation> cls;

        CommandType(Class<? extends Annotation> cls) {
            this.cls = cls;
        }

        public abstract void apply(CommandWrapper wrapper, Object instance, Method method);

    }


}
