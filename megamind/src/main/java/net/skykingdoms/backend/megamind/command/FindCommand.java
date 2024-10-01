package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.WithMultipleAliases;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@WithMultipleAliases({
        @WithAlias("search"),
        @WithAlias("find"),
        @WithAlias("stalk"),
        @WithAlias("whereareyou")
})
@NamedCommand("find")
public class FindCommand {

    @Executor
    @WithCondition("group: admin")
    @WithCondition("minArgs: 1")
    public static void execute(CoreExecutionSession session) {
        String username = session.getArgument(0);
        Gamer gamer = Gamer.getGamer(username);

        if (gamer == null) {
            session.sendMessage("Gamer not found: " + username);
            return;
        }

        if (gamer.getCurrentServer() == null) {
            session.sendMessage("§5Beep, boop, beep! ~~~\n§8" + gamer.getName() + " is currently nowhere to be found.");
            return;
        }


        session.sendMessage("§5Beep, boop, beep! ~~~\n§8" + gamer.getName() + " is currently on server: " + gamer.getCurrentServer().getName());
    }
}
