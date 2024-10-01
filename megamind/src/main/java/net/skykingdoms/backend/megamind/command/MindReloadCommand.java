package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

@NamedCommand("reloadmind")
public class MindReloadCommand {
    @Executor
    @WithCondition("group: owner")
    public static void execute(CoreExecutionSession session) {
        MegamindApp.INSTANCE.pluginManager();
        MegamindApp.INSTANCE.enablePlugins();
        session.sendMessage("Reloaded plugins");
    }
}
