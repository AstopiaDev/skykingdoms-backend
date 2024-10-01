package net.skykingdoms.backend.megamind.command;

import com.google.gson.JsonObject;
import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

/**
 * @author Vihara
 */
@NamedCommand("locraw")
public final class LocationRawCommand {

    @WithCondition("disableConsole")
    @Executor
    public static void execute(final CoreExecutionSession session) {
        val gamer = session.getGamer();
        val json = new JsonObject();

        // for lobbies, we want to see only dynamiclobby component as server name
        // while for gameserver instances it's necessary to see the instance full name
        if (gamer.getLobbyServer() != null) {
            json.addProperty("server", gamer.getBukkitServer().getName());
        } else {
            json.addProperty("server", gamer.getCurrentServer().getName());
        }

        if (gamer.getBukkitServer().getGameType() != null) {
            json.addProperty("gametype", gamer.getBukkitServer().getGameType().name());
        }

        if (gamer.getLobbyServer() != null) {
            json.addProperty("lobbyname", gamer.getLobbyServer().getName());
        }

        if (gamer.getGameServer() != null) {
            json.addProperty("mode", gamer.getGameServer().getGameMode().name());
            json.addProperty("map", gamer.getGameServer().getMap());
        }

        gamer.sendMessage(json.toString());
    }

}
