package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;

/**
 * @author Vihara
 */
@NamedCommand("connect")
public final class ConnectCommand {

    @Tabcomplete("[connectable]")
    @WithCondition("disableConsole")
    @WithCondition("group: admin")
    @WithCondition("minArgs: 1")
    @Executor
    public static void execute(final CoreExecutionSession session) throws CommandMessageException {
        val target = (AbstractPlayerHolder) ServerManager.INSTANCE.getPlayableServer(session.getArgument(0));
        if (target == null) {
            throw new CommandMessageException("Invalid target");
        }

        val gamer = session.getGamer();
        session.sendMessage(gamer.sendTo(target).name());
    }

}
