package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.AbstractScalable;

@WithAlias("strezniki")
@NamedCommand("allservers")
public class AllServersCommand {

    @Executor
    @WithCondition("group: owner")
    public static void execute(CoreExecutionSession session) {
        StringBuilder builder = new StringBuilder();
        builder.append("All servers: \n");
        for (AbstractScalable scalable : ServerManager.INSTANCE.getServers()) {
            builder.append(scalable.getName()).append(", ");
        }

        session.sendMessage(builder.toString());
    }
}
