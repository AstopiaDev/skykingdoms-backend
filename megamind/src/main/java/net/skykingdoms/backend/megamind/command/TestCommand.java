package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

@NamedCommand("testevent")
public class TestCommand {
    @Executor
    public static void execute(CoreExecutionSession session) {
        session.sendMessage("Sending test event");
        MegamindApp.INSTANCE.pluginManager().callEvent(new GamerJoinEvent(session.getGamer()));
    }
}
