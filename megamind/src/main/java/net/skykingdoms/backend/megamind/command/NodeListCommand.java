package net.skykingdoms.backend.megamind.command;

import com.google.common.collect.Streams;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.NodeManager;
import net.skykingdoms.backend.megamind.manager.ProxyManager;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.impl.GameServer;
import net.skykingdoms.backend.megamind.server.impl.ProxyServer;

import java.util.stream.Collectors;

/**
 * @author Vihara
 */
@NamedCommand("nodelist")
public final class NodeListCommand {

    @WithCondition("group: admin")
    
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) {
        val nodes = NodeManager.INSTANCE.getRegisteredNodes();
        if (nodes.size() == 0) {
            session.sendMessage("No nodes are registered yet!");
            return;
        }

        val sb = new StringBuilder();
        sb.append("List of registered nodes [").append(nodes.size()).append("]:\n");

        for (val node : nodes) {
            val servers = ServerManager.INSTANCE.getServers();
            val proxies = ProxyManager.INSTANCE.getProxyServers();
            val cocked = Streams.concat(
                    servers.stream(),
                    proxies.values().stream()
                            .filter(p -> node == p.getNode())
                    ).distinct().collect(Collectors.toList());

            sb.append(node.getName()).append(" (S: ").append(cocked.size()).append(" - P: ").append(cocked.stream()
                    .filter(s -> s instanceof ProxyServer)
                    .mapToInt(s -> s.getPlayerIds().length) // counting only all players connected to PROXIES
                    .reduce(Integer::sum)
                    .orElse(0)).append(")");

            if (cocked.size() > 0) sb.append(":\n");

            for (val cock : cocked) {
                val gameData = cock instanceof GameServer ? ((GameServer) cock).getGameMode() + "/" + ((GameServer) cock).getMap() : null;
                sb.append("-> ").append(cock.getName()).append(gameData != null ? " [" + gameData + "]" : "").append(" (").append(cock.getPlayerIds().length).append(")").append("\n");
            }

            sb.append("\n");
        }

        session.sendMessage(sb.toString());
    }

}
