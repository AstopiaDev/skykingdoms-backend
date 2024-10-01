package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
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
