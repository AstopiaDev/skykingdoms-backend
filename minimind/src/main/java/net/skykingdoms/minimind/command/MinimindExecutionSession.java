package net.skykingdoms.minimind.command;

import lombok.NonNull;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.gamer.ICommandSender;
import net.skykingdoms.backend.base.gamer.IGamer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MinimindExecutionSession extends ExecutionSession {

    CommandSender sender;
    AbstractGamer gamer;

    public AbstractGamer getOfflineGamer(final int i, final @NonNull Consumer<AbstractGamer> loader) throws CommandTranslationException {
        AbstractGamer gamer = AbstractGamer.getOfflineGamer(getArgument(i));

        if (gamer == null) {
            throw new CommandTranslationException("player_not_found");
        }

        return gamer;
    }

    public AbstractGamer getGamer(final int i) throws CommandTranslationException {
        AbstractGamer gamer = AbstractGamer.getGamer(getArgument(i));

        if (gamer == null) {
            throw new CommandTranslationException("player_offline");
        }

        return gamer;
    }

    public MinimindExecutionSession(final @NonNull WrappedCommandExecutor executor,
                                final @NonNull CommandSender sender,
                                final @Nullable AbstractGamer gamer,
                                final String @NonNull [] arguments) {
        super(executor, arguments);

        this.sender = sender;
        this.gamer = gamer;
    }

    @Override
    public ICommandSender getSender() {
        return sender;
    }

    @Override
    public IGamer getGamer() {
        return gamer;
    }

    @Override
    public void sync(Runnable runnable) {
        runnable.run();
    }
}
