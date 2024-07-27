package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

@NamedCommand("shutdown")
public class ShutdownCommand {
    @Executor
    @WithCondition("consoleOnly")
    public static void execute(CoreExecutionSession session) {
        MegamindApp.INSTANCE.shutdown();
    }
}
