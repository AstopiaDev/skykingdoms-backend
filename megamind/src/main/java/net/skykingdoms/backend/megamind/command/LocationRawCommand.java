package net.skykingdoms.backend.megamind.command;

import com.google.gson.JsonObject;
import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.instance.impl.ArenaInstance;
import net.skykingdoms.backend.megamind.instance.impl.LobbyInstance;

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

        // for lobbies, we want to see only dynamiclobby component as instance name
        // while for gameserver instances it's necessary to see the instance full name
        if (gamer.getBukkitInstance() instanceof LobbyInstance) {
            json.addProperty("lobbyname", gamer.getBukkitInstance().getName());
        } else {
            json.addProperty("instance", gamer.getCurrentServer().getName());
        }

        if (gamer.getBukkitInstance().getGameType() != null) {
            json.addProperty("gametype", gamer.getBukkitInstance().getGameType().name());
        }


        if (gamer.getBukkitInstance() instanceof ArenaInstance) {
            ArenaInstance arenaInstance = (ArenaInstance) gamer.getBukkitInstance();
            json.addProperty("mode", arenaInstance.getGameMode().name());
            json.addProperty("map", arenaInstance.getMap());
        }

        gamer.sendMessage(json.toString());
    }

}
