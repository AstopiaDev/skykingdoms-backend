package net.skykingdoms.backend.base.command;

import lombok.*;
import net.skykingdoms.api.backend.command.CommandExecutor;
import net.skykingdoms.api.backend.command.ICommand;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.exception.CommandRelayException;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import net.skykingdoms.api.backend.gamer.IGamer;
import net.skykingdoms.backend.base.command.condition.WrappedSubcommandExecutor;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;
import net.skykingdoms.backend.base.command.execution.help.HelpCommandExecutor;
import net.skykingdoms.backend.base.command.tabcomplete.StaticTabcompleter;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcompleter;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author Vihara
 */
@ToString(exclude = "commandManager")
@RequiredArgsConstructor
@Getter
public class CommandWrapper implements ICommand {

    private final CommandManager commandManager;

    private final String name;

    private final String[] aliases;

    @Setter
    private CommandExecutor<ExecutionSession> prepareSession;

    @Setter
    private WrappedCommandExecutor fallback;

    private WrappedCommandExecutor executor;

    private Tabcompleter[] subcommandCompleters;
    private Tabcompleter[] cachedCompleters;

    private final Map<String, WrappedSubcommandExecutor> subcommands = new LinkedHashMap<>();

    public void setExecutor(WrappedCommandExecutor executor) {
        if (executor instanceof HelpCommandExecutor) {
            if (this.executor != null) {
                this.fallback = this.executor;
            }
        } else {
            if (this.executor instanceof HelpCommandExecutor) {
                this.fallback = executor;

                return;
            }
        }

        this.executor = executor;
    }

    public void addSubcommand(WrappedSubcommandExecutor executor) {
        for (String name : executor.getNames()) {
            subcommands.put(name.toLowerCase(), executor);
        }

        // ну на всякий случай :)
        subcommandCompleters = null;
        cachedCompleters = null;
    }

    private boolean doChecked(CommandExecutor<ExecutionSession> sessionConsumer, ExecutionSession session) throws Exception {
        try {
            sessionConsumer.execute(session);

            return false;
        } catch (CommandMessageException e) {
            e.printMessages(session);

            return true;
        }
    }

    public boolean hasFallback() {
        return fallback != null;
    }

    public boolean hasExecutor() {
        return executor != null;
    }

    public WrappedCommandExecutor getExecutor(String[] args) {
        WrappedCommandExecutor executor = args.length == 0
                ? CommandWrapper.this.executor
                : subcommands.get(args[0].toLowerCase());

        if (executor == null) {
            executor = hasFallback() ? fallback : CommandWrapper.this.executor;
        }

        return executor;
    }

    public void run(ICommandSender sender, IGamer gamer, String[] args, WrappedCommandExecutor executor) {
        try {
            if (executor == null) {
                sender.sendMessage("No executor found");
                return;
            }

            if (executor instanceof WrappedSubcommandExecutor) {
                args = Arrays.copyOfRange(args, 1, args.length);
            }

            ExecutionSession session = commandManager.newCommandSession(executor, sender, gamer, args);

            if (prepareSession != null) {
                doChecked(prepareSession, session);
            }

            for (val condition : executor.getConditions()) {
                if (doChecked(condition.getChecker(), session)) {
                    return;
                }
            }

            doChecked(executor.getExecutor(), session);
        } catch (CommandRelayException relay) {
            sender.dispatchCommand(relay.getRelay());
        } catch (Throwable t) {
            t.printStackTrace();

            sender.sendMessage("§cInternal error occurred while performing this command.");
        }
    }

    @Override
    public void execute(final @NonNull ICommandSender sender,
                        final String @NonNull [] args) {
        val gamer = sender.isConsole() ? null : (IGamer) sender;

        val executor = getExecutor(args);

        ExecutorService threadExecutor = executor == null ? null : executor.getThreadExecutor();
        Runnable runnable = () -> run(sender, gamer, args, executor);

        if (threadExecutor == null) {
            runnable.run();
        } else {
            threadExecutor.submit(runnable);
        }
    }

    public Tabcompleter[] getSubcommandCompleters() {
        if (subcommandCompleters == null) {
            subcommandCompleters = new Tabcompleter[]{
                    new StaticTabcompleter(subcommands.keySet().toArray(new String[0]))
            };
        }

        return subcommandCompleters;
    }

    public Tabcompleter[] getCachedCompleters() {
        if (cachedCompleters == null) {
            if (hasExecutor() && executor.hasTabcompleters()) {
                cachedCompleters = executor.getTabcompleters();
            } else if (hasFallback() && fallback.hasTabcompleters()) {
                cachedCompleters = fallback.getTabcompleters();
            }

            if (subcommands.size() > 0) {
                cachedCompleters = merge(cachedCompleters, getSubcommandCompleters());
            }
        }

        return cachedCompleters;
    }

    private Tabcompleter[] merge(Tabcompleter[] first, Tabcompleter[] second) {
        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        Tabcompleter[] newTabcompleters = new Tabcompleter[Math.max(first.length, second.length)];

        for (int i = 0; i < newTabcompleters.length; i++) {
            Tabcompleter a = i >= first.length ? null : first[i];
            Tabcompleter b = i >= second.length ? null : second[i];

            newTabcompleters[i] = a == null ? b : b == null ? a : a.with(b);
        }

        return newTabcompleters;
    }

    @Override
    public List<String> tabcomplete(ICommandSender sender, String[] args) {
        int offset = 0;
        Tabcompleter[] tabcompleters = null;

        if (args.length < 2) {
            tabcompleters = getCachedCompleters();
        } else {
            WrappedSubcommandExecutor subcommand = subcommands.get(args[0].toLowerCase());

            if (subcommand != null && subcommand.hasTabcompleters()) {
                tabcompleters = subcommand.getTabcompleters();
                offset = 1;
            } else if (hasFallback() && fallback.hasTabcompleters()) {
                tabcompleters = fallback.getTabcompleters();
            } else if (hasExecutor() && executor.hasTabcompleters()) {
                tabcompleters = executor.getTabcompleters();
            }
        }

        if (tabcompleters == null || args.length - offset > tabcompleters.length) {
            return Collections.emptyList();
        }

        int argIndex = args.length - 1;
        List<String> result = tabcompleters[argIndex - offset].complete(sender, args[argIndex]);

        return result != null ? result : Collections.emptyList();
    }

}
