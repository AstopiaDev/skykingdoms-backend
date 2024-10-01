package net.skykingdoms.backend.velocity.command;

import com.velocitypowered.api.command.CommandMeta;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.command.ICommand;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import net.skykingdoms.api.backend.gamer.IGamer;
import net.skykingdoms.backend.base.command.CommandManager;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;
import net.skykingdoms.backend.velocity.VelocityConnector;
import net.skykingdoms.backend.velocity.gamer.Gamer;
import org.jetbrains.annotations.Nullable;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class VelocityCommandManager extends CommandManager {
    @Deprecated
    String prefix = "SkyKingdoms";

    public VelocityCommandManager() {
        super();
    }

    @Override
    public void registerCommand(final @NonNull String name,
                                final @NonNull ICommand command) {
        if (name.equals(command.getName())) {
            // We register only primary command name
            // the aliases will be registered automatically by bungee
            com.velocitypowered.api.command.CommandManager manager = VelocityConnector.INSTANCE.server().getCommandManager();
            CommandMeta meta = manager
                    .metaBuilder(name)
                    .build();

            VelocityConnector.INSTANCE.server().getCommandManager().register(meta, new CommandProxy(command));
        }
    }

    @Override
    public String getServerName() {
        return "Bungee";
    }

    @Override
    public ExecutionSession newCommandSession(final @NonNull WrappedCommandExecutor executor,
                                              final @NonNull ICommandSender sender,
                                              final @Nullable IGamer gamer,
                                              final String @NonNull [] args) {
        return new VelocityExecutionSession(prefix, executor, (ProxiedCommandSender) sender,
                gamer == null ? null : (Gamer) gamer, args);
    }

}
