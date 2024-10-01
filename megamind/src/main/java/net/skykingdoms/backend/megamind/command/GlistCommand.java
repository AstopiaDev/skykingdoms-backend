package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.WithMultipleAliases;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WithMultipleAliases({
        @WithAlias("globallist"),
        @WithAlias("hackers"),
        @WithAlias("people"),
        @WithAlias("gamers"),
        @WithAlias("minecrafters")
})
@NamedCommand("glist")
public class GlistCommand {
    @Executor
    @WithCondition("group: admin")
    public static void execute(CoreExecutionSession session) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("§e§lMinecrafters are better than Gamers\n");
        stringBuilder.append("§7§oPlayers online: ").append(Gamer.getGamers().size()).append("\n");

        Map<AbstractPlayerHolder, List<String>> servers = new HashMap<>();
        Gamer.getGamers().forEach(gamer -> {
            AbstractPlayerHolder server = gamer.getCurrentServer();
            if (server == null) {
                return;
            }
            servers.computeIfAbsent(server, k -> new ArrayList<>()).add(gamer.getName());
        });
        for (AbstractPlayerHolder server : servers.keySet()) {
            stringBuilder.append("§f§l").append(server.getName()).append(" (").append(server.getPlayerIds().length).append(")").append(": ");
            for (String name : servers.get(server)) {
                stringBuilder.append("§7").append(name).append(", ");
            }
            stringBuilder.append("\n");
        }

        session.sendMessage(stringBuilder.toString());
    }
}
