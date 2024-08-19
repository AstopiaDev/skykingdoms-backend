package net.skykingdoms.backend.megamind.command;

import com.google.common.collect.Streams;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.NodeManager;
import net.skykingdoms.backend.megamind.manager.ProxyManager;
import net.skykingdoms.backend.megamind.manager.InstanceManager;
import net.skykingdoms.backend.megamind.instance.impl.GameInstance;
import net.skykingdoms.backend.megamind.instance.impl.ProxyInstance;

import java.util.stream.Collectors;

/**
 * @author Vihara
 */
@NamedCommand("nodelist")
public final class NodeListCommand {

    @WithCondition("group: admin")
    
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) {
/*        val nodes = NodeManager.INSTANCE.getRegisteredNodes();
        if (nodes.size() == 0) {
            session.sendMessage("No nodes are registered yet!");
            return;
        }

        val sb = new StringBuilder();
        sb.append("List of registered nodes [").append(nodes.size()).append("]:\n");

        for (val node : nodes) {
            val servers = InstanceManager.INSTANCE.getServers();
            val proxies = ProxyManager.INSTANCE.getProxyInstances();

            val cocked = Streams.concat(
                    servers.stream(),
                    proxies.stream()
                            .filter(p -> node == p.getNode())
                    ).distinct().collect(Collectors.toList());

            sb.append(node.getName()).append(" (S: ").append(cocked.size()).append(" - P: ").append(cocked.stream()
                    .filter(s -> s instanceof ProxyInstance)
                    .mapToInt(s -> s.getPlayerIds().length) // counting only all players connected to PROXIES
                    .reduce(Integer::sum)
                    .orElse(0)).append(")");

            if (cocked.size() > 0) sb.append(":\n");

            for (val cock : cocked) {
                val gameData = cock instanceof GameInstance ? ((GameInstance) cock).getGameMode() + "/" + ((GameInstance) cock).getMap() : null;
                sb.append("-> ").append(cock.getName()).append(gameData != null ? " [" + gameData + "]" : "").append(" (").append(cock.getPlayerIds().length).append(")").append("\n");
            }

            sb.append("\n");
        }

        session.sendMessage(sb.toString());*/
    }

}
