package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;

@NamedCommand("build")
public class BuildCommand {
    @WithCondition("disableConsole")
    @Executor
    public static void execute(final CoreExecutionSession session) throws CommandMessageException {
        val target = (AbstractPlayerHolder) ServerManager.INSTANCE.getPersistentServer(session.getGamer(), GameMode.BUILD);
        if (target == null) {
            throw new CommandMessageException("Invalid target");
        }

        val gamer = session.getGamer();
        session.sendMessage(gamer.sendTo(target).name());
    }

}

