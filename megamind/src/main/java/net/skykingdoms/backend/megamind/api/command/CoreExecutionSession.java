package net.skykingdoms.backend.megamind.api.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class CoreExecutionSession extends ExecutionSession {

    CommandSender sender;
    Gamer gamer;

    public Gamer getOfflineGamer(final int i, final @NonNull Consumer<Gamer> loader) throws CommandTranslationException {
        Gamer gamer = Gamer.getOfflineGamer(getArgument(i), loader);

        if (gamer == null) {
            throw new CommandTranslationException("player_not_found");
        }

        return gamer;
    }

    public Gamer getGamer(final int i) throws CommandTranslationException {
        Gamer gamer = Gamer.getGamer(getArgument(i));

        if (gamer == null) {
            throw new CommandTranslationException("player_offline");
        }

        return gamer;
    }

    public CoreExecutionSession(final @NonNull WrappedCommandExecutor executor,
                                final @NonNull CommandSender sender,
                                final @Nullable Gamer gamer,
                                final String @NonNull [] arguments) {
        super(executor, arguments);

        this.sender = sender;
        this.gamer = gamer;
    }

    @Override
    public void sync(Runnable runnable) {
        runnable.run();
    }
}
