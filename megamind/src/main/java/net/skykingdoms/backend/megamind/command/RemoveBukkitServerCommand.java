package net.skykingdoms.backend.megamind.command;

import lombok.val;
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
        var force = false;

        for (val arg : session.getArguments()) {
            if (arg.equalsIgnoreCase("-f")) force = true;
        }

        ServerManager.INSTANCE.removeBukkitServer(name, true, force);
        session.sendMessage("Removing bukkit server: {} - Flags: F({})", name, force);
    }
}
