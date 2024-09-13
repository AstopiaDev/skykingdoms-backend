package net.skykingdoms.minimind.command;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.command.CommandManager;
import net.skykingdoms.backend.base.command.ICommand;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.gamer.ICommandSender;
import net.skykingdoms.backend.base.gamer.IGamer;
import net.skykingdoms.minimind.Minimind;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class MinimindCommandManager extends CommandManager {
    Minimind minimind;

    public MinimindCommandManager(final @NonNull Minimind minimind) {
        this.minimind = minimind;
    }

    @Override
    public void registerCommand(String name, ICommand command) {
        minimind.registerCommand(name, command);
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public ExecutionSession newCommandSession(@NonNull WrappedCommandExecutor executor, @NonNull ICommandSender sender, @NonNull IGamer gamer, @NotNull @NonNull String[] args) {
        return new MinimindExecutionSession(executor, (CommandSender) sender, gamer == null ? null : (AbstractGamer) gamer, args);
    }
}
