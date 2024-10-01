package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.execution.SubcommandExecutor;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.manager.DuelManager;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.protocol.type.duel.KitFluentRequestData;

import java.util.concurrent.TimeUnit;

@NamedCommand("duelinternal")
public class DuelInternalCommand {
    @Executor
    @WithCondition("minArgs: 2")
    @WithCondition("disableConsole")
    public static void execute(CoreExecutionSession session) {
        var gamer = session.getGamer();
        var target = Gamer.getGamer(session.getArgument(0));

        if (DuelManager.INSTANCE.requestExists(gamer.getGamerId(), target.getGamerId())) {
            gamer.sendMessage("§cYou already have a pending request with this player.");
            return;
        }

        if (target == null || !target.isOnline()) {
            gamer.sendMessage("§cPlayer not found.");
            return;
        }

        if (session.getArgumentCount() > 1 && session.getArgumentCount() < 3) {
            var duel = session.getArgument(1);
        } else if (session.getArgumentCount() == 3) {
            var duel = session.getArgument(1);
            var map = session.getArgument(2);

            if (!gamer.getRanks().canApply(Group.INFINITY)) {
                gamer.sendMessage("§cOnly players with §5Infinity§c or higher can select maps.");
                return;
            }
        } else if (session.getArgumentCount() == 4) {
            var duel = session.getArgument(1);
            var map = session.getArgument(2);
            var kit = session.getArgument(3);

/*            if (!map.equals("null")) {
                if (!gamer.getRanks().canApply(Group.INFINITY)) {
                    gamer.sendMessage("§cOnly players with §5Infinity§c or higher can select maps.");
                    return;
                }
            }*/

            GameMode gameMode = GameMode.valueOf(duel.toUpperCase(), null);

            if (gameMode == null) {
                return;
            }

            val data = new KitFluentRequestData(map, kit);
            DuelManager.INSTANCE.addRequest(gamer.getGamerId(), target.getGamerId(), gameMode, data);

            gamer.sendMessage("§aDuel request sent. Expires in 60 seconds.");
            target.sendRawMessage(
                    target.getLanguage().getMessage(target.isModern(), "duel_invite_received",
                            "%playerDisplay%", gamer.getDisplayName(),
                            "%player%", gamer.getName(),
                            "%mode%", gameMode.getHumanName(),
                            "%kit%", kit)
            );
        }
    }

    @SubcommandExecutor("accept")
    @WithCondition("disableConsole")
    @WithCondition("minArgs: 1")
    public static void executeAccept(CoreExecutionSession session) {
        var gamer = session.getGamer();
        val sender = Gamer.getGamer(session.getArgument(0));

        if (sender == null || !sender.isOnline()) {
            session.sendMessage("§cPlayer not found.");
            return;
        }

        val duel = DuelManager.INSTANCE.getRequest(sender.getGamerId());

        if (duel == null || duel.getTargetId() != gamer.getGamerId()) {
            session.sendMessage("§cNo pending invitation.");
            return;
        }

        val data = duel.getData();

        if (data == null || duel.getMode() == null) {
            session.sendMessage("§cNo pending invitation.");
            return;
        }

        sender.sendMessage("§a" + gamer.getDisplayName() + " §ahas accepted your duel request.");
        sender.sendMessage("§7Searching for a server, please wait ...");
        gamer.sendMessage("§aYou have accepted the duel invite from " + sender.getDisplayName());
        gamer.sendMessage("§7Searching for a server, please wait ...");


        DuelManager.INSTANCE.removeRequest(sender.getGamerId());
        String serverName = DuelManager.INSTANCE.getAppropriateServer(sender, duel.getMode(), data);

        AsyncExecution.INSTANCE.runLater(() -> {
            if (serverName == null) {
                gamer.sendMessage("§cNo server available, try again later.");
                sender.sendMessage("§cNo server available, try again later.");
                return;
            }

            AbstractScalable server = ServerManager.INSTANCE.getPlayableServer(serverName);

            if (server != null) {
                gamer.sendTo(server);
                sender.sendTo(server);
            } else {
                gamer.sendMessage("§cNo server available, try again later.");
                sender.sendMessage("§cNo server available, try again later.");
            }
        }, 500, TimeUnit.MILLISECONDS);
    }
}
