package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.ServerManager;

@NamedCommand("removebukkitserver")
public class RemoveBukkitServerCommand {
    @Executor
    @WithCondition("group: owner")
    @WithCondition("minArgs: 1")
    public static void execute(CoreExecutionSession session) {
        String name = session.getArgument(0);
        ServerManager.INSTANCE.removeBukkitServer(name, true);
        session.sendMessage("Removing bukkit server: {}", name);
    }
}
