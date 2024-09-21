package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.exception.CommandMessageException;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;
import net.skykingdoms.backend.api.protocol.type.GameMode;

@NamedCommand("skypvp")
public class SkypvpCommand {
    @WithCondition("disableConsole")
    @Executor
    public static void execute(final CoreExecutionSession session) throws CommandMessageException {
        val target = (AbstractPlayerHolder) ServerManager.INSTANCE.getPersistentServer(session.getGamer(), GameMode.SKYPVP);
        if (target == null) {
            throw new CommandMessageException("Invalid target");
        }

        val gamer = session.getGamer();
        session.sendMessage(gamer.sendTo(target).name());
    }

}



