package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.exception.CommandMessageException;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.InstanceManager;
import net.skykingdoms.backend.megamind.instance.AbstractPlayerHolder;

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
        val target = (AbstractPlayerHolder) InstanceManager.INSTANCE.getPlayableServer(session.getArgument(0));
        if (target == null) {
            throw new CommandMessageException("Invalid target");
        }

        val gamer = session.getGamer();
        session.sendMessage(gamer.sendTo(target).name());
    }

}
