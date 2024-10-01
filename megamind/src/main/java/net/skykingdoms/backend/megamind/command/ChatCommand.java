package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.RelayExecutor;
import net.skykingdoms.api.backend.command.RelayExecutorAlias;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.SubcommandExecutor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@NamedCommand("chat")
public class ChatCommand {

    @RelayExecutorAlias({
            "togglepc",
    })
    @RelayExecutor("togglepartychat")
    @WithAlias("p")
    @SubcommandExecutor("party")
    @WithCondition("disableConsole")
    public static void executeParty(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        if (gamer.getPropertyOrDefault("chat_mode", 0) == 1) {
            gamer.setProperty("chat_mode", 0);
            session.sendMessage("§7Party chat disabled");
        } else {
            gamer.setProperty("chat_mode", 1);
            session.sendMessage("§7Party chat enabled");
        }
    }

    @RelayExecutorAlias({
            "togglesc",
    })
    @RelayExecutor("togglestaffchat")
    @WithAlias("s")
    @SubcommandExecutor("staff")
    @WithCondition("group: helper")
    @WithCondition("disableConsole")
    public static void executeStaff(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        if (gamer.getPropertyOrDefault("chat_mode", 0) == 2) {
            gamer.setProperty("chat_mode", 0);
            session.sendMessage("§7Staff chat disabled");
        } else {
            gamer.setProperty("chat_mode", 2);
            session.sendMessage("§7Staff chat enabled");
        }
    }
}
