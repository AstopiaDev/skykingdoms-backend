package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.api.megamind.event.impl.gamer.GamerJoinEvent;

@NamedCommand("testevent")
public class TestCommand {
    @Executor
    public static void execute(CoreExecutionSession session) {
        session.sendMessage("Sending test event");
        MegamindApp.INSTANCE.pluginManager().callEvent(new GamerJoinEvent(session.getGamer()));
    }
}
