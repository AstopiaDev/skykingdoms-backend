package net.skykingdoms.backend.tcpshield.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.execution.SubcommandExecutor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.tcpshield.manager.TcpShieldManager;

@NamedCommand("tcpbackends")
public final class TcpShieldBackendsCommand {

    @WithCondition("group: admin")
    @Executor
    public static void execute(final CoreExecutionSession session) {
        session.sendMessage("/tcpbackends list - list loaded backends (locally)");
        session.sendMessage("/tcpbackends update-backend - reload backends (pull from remote)");
    }


    @WithCondition("group: admin")
    @SubcommandExecutor("list")
    public static void listBackends(final CoreExecutionSession session) {
        val backends = TcpShieldManager.INSTANCE.getLoadedBackends();
        session.sendMessage("In total {} backends loaded (local){}", backends.size(), !backends.isEmpty() ? ":" : "");
        for (val backend : backends) {
           // session.sendMessage("{} -> {}:{}", backend.getId(), backend.getIp(), backend.getPort());
        }
    }

    @WithCondition("group: admin")
    @SubcommandExecutor("update-backend")
    public static void updateBackends(final CoreExecutionSession session) {
        session.sendMessage("Pulling backends from NeoProtect API ...");
        TcpShieldManager.INSTANCE.reloadBackends();
        session.sendMessage("Done pulling backends from remote!");

        listBackends(session); // retarded, but for this time it's ok
    }

}
