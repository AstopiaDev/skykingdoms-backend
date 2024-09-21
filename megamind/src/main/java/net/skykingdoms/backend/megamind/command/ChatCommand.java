package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.RelayExecutor;
import net.skykingdoms.backend.base.command.RelayExecutorAlias;
import net.skykingdoms.backend.base.command.WithAlias;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.execution.SubcommandExecutor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.api.protocol.type.PropertyType;

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
        if (gamer.getProperty(PropertyType.CHAT_MODE) == 1) {
            gamer.setProperty(PropertyType.CHAT_MODE, 0);
            session.sendMessage("§7Party chat disabled");
        } else {
            gamer.setProperty(PropertyType.CHAT_MODE, 1);
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
        if (gamer.getProperty(PropertyType.CHAT_MODE) == 2) {
            gamer.setProperty(PropertyType.CHAT_MODE, 0);
            session.sendMessage("§7Staff chat disabled");
        } else {
            gamer.setProperty(PropertyType.CHAT_MODE, 2);
            session.sendMessage("§7Staff chat enabled");
        }
    }
}
