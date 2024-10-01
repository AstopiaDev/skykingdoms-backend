package net.skykingdoms.minimind.command;

import lombok.NonNull;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class MinimindExecutionSession extends ExecutionSession {

    CommandSender sender;
    CommonGamer gamer;

    public CommonGamer getOfflineGamer(final int i, final @NonNull Consumer<CommonGamer> loader) throws CommandTranslationException {
        CommonGamer gamer = CommonGamer.getOfflineGamer(getArgument(i));

        if (gamer == null) {
            throw new CommandTranslationException("player_not_found");
        }

        return gamer;
    }

    public CommonGamer getGamer(final int i) throws CommandTranslationException {
        CommonGamer gamer = CommonGamer.getGamer(getArgument(i));

        if (gamer == null) {
            throw new CommandTranslationException("player_offline");
        }

        return gamer;
    }

    public MinimindExecutionSession(final @NonNull WrappedCommandExecutor executor,
                                final @NonNull CommandSender sender,
                                final @Nullable CommonGamer gamer,
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
    public CommonGamer getGamer() {
        return gamer;
    }

    @Override
    public void sync(Runnable runnable) {
        runnable.run();
    }
}
