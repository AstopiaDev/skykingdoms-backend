package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

/**
 * @author Vihara
 */
@NamedCommand("proxy")
public final class ProxyCommand {

    @WithCondition("disableConsole")
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) {
        session.sendMessage(session.getGamer().getProxyServer().getName());
    }

}
