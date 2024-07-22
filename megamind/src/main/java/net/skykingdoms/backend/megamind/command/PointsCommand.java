/*
package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.WithAlias;
import net.skykingdoms.backend.base.command.WithMultipleAliases;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.SubcommandExecutor;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.type.PropertyType;

@WithMultipleAliases({@WithAlias("p"), @WithAlias("point")})
@NamedCommand("points")
public class PointsCommand {
    @WithCondition("disableConsole")
    @SubcommandExecutor("me")
    public static void execute(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        session.sendMessage("§aYou have §e" + gamer.getProperty(PropertyType.PLAYER_POINTS) + "§a points.");
    }

    @SubcommandExecutor("look")
    @WithCondition("minArgs: 1")
    public static void executePoints(CoreExecutionSession session) {
        int id = NetworkSqlHandler.INSTANCE.getPlayerId(session.getArgument(0));
        Gamer gamer = Gamer.getOfflineGamer(id, AbstractGamer::loadProperties);
        if (gamer == null) {
            session.sendMessage("§cPlayer not found.");
            return;
        }

        session.sendMessage("§a" + gamer.getName() + " has §e" + gamer.getProperty(PropertyType.PLAYER_POINTS) + "§a points.");
    }

    @SubcommandExecutor("pay")
    @WithCondition("disableConsole")
    @WithCondition("minArgs: 2")
    public static void executePay(CoreExecutionSession session) {
        String username = session.getArgument(0);
        int amount = Integer.parseInt(session.getArgument(1));


        int id = NetworkSqlHandler.INSTANCE.getPlayerId(username);
        Gamer target = Gamer.getGamer(id);

        if (target == null) {
            session.sendMessage("§cPlayer not found.");
            return;
        }

        Gamer gamer = session.getGamer();
        int points = gamer.getProperty(PropertyType.PLAYER_POINTS);

        if (points < amount) {
            session.sendMessage("§cYou don't have enough points.");
            return;
        }

        gamer.sendMessage("§aYou have paid §e" + amount + "§a points to §e" + target.getName() + "§a.");
        gamer.setProperty(PropertyType.PLAYER_POINTS, points - amount);
        target.setProperty(PropertyType.PLAYER_POINTS, target.getProperty(PropertyType.PLAYER_POINTS) + amount);
        target.sendMessage("§aYou have received §e" + amount + "§a points from §e" + gamer.getName() + "§a.");
    }

    @SubcommandExecutor("give")
    @WithCondition("minArgs: 2")
    @WithCondition("group: owner")
    public static void executeGive(CoreExecutionSession session) {
        String username = session.getArgument(0);
        int amount = Integer.parseInt(session.getArgument(1));

        int id = NetworkSqlHandler.INSTANCE.getPlayerId(username);
        Gamer target = Gamer.getGamer(id);

        if (target == null) {
            session.sendMessage("§cPlayer not found.");
            return;
        }

        session.sendMessage("§aYou have given §e" + amount + "§a points to §e" + target.getName() + "§a.");
        target.setProperty(PropertyType.PLAYER_POINTS, target.getProperty(PropertyType.PLAYER_POINTS) + amount);
        target.sendMessage("§aYou have received §e" + amount + "§a points.");
    }


    @SubcommandExecutor("take")
    @WithCondition("minArgs: 2")
    @WithCondition("group: owner")
    public static void executeTake(CoreExecutionSession session) {
        String username = session.getArgument(0);
        int amount = Integer.parseInt(session.getArgument(1));

        int id = NetworkSqlHandler.INSTANCE.getPlayerId(username);
        Gamer target = Gamer.getGamer(id);

        if (target == null) {
            session.sendMessage("§cPlayer not found.");
            return;
        }

        if ((target.getProperty(PropertyType.PLAYER_POINTS) - amount) < 0) {
            session.sendMessage("§cDon't make them go bankrupt bro, they're going to go into the negatives.");
            return;
        }

        session.sendMessage("§aYou have taken §e" + amount + "§a points from §e" + target.getName() + "§a.");
        target.setProperty(PropertyType.PLAYER_POINTS, target.getProperty(PropertyType.PLAYER_POINTS) - amount);
    }


}
*/
