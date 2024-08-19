package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.WithAlias;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.InstanceManager;
import net.skykingdoms.backend.megamind.instance.AbstractScalable;

@WithAlias("strezniki")
@NamedCommand("allservers")
public class AllServersCommand {

    @Executor
    @WithCondition("group: owner")
    public static void execute(CoreExecutionSession session) {
        StringBuilder builder = new StringBuilder();
        builder.append("All servers: \n");
        for (AbstractScalable scalable : InstanceManager.INSTANCE.getInstances()) {
            builder.append(scalable.getName()).append(", ");
        }

        session.sendMessage(builder.toString());
    }
}
