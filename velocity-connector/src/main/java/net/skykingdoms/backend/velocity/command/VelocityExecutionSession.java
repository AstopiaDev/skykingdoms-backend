package net.skykingdoms.backend.velocity.command;

import com.velocitypowered.api.proxy.Player;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;
import net.skykingdoms.backend.velocity.gamer.Gamer;
import org.jetbrains.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class VelocityExecutionSession extends ExecutionSession {

    String prefix;

    ProxiedCommandSender sender;
    Gamer gamer;

    Player player;

    public VelocityExecutionSession(final @NonNull String prefix,
                                    final @NonNull WrappedCommandExecutor executor,
                                    final @NonNull ProxiedCommandSender sender,
                                    final @Nullable Gamer gamer,
                                    final String @NonNull [] arguments) {
        super(executor, arguments);

        this.prefix = prefix;
        this.sender = sender;
        this.gamer = gamer;
        this.player = gamer == null ? null : gamer.getPlayer();
    }

    @Override
    public void sendPrefixedMessage(String message) {
        this.sendMessage(prefix + message);
    }

    @Override
    public void sync(Runnable runnable) {
        runnable.run();
    }
}
