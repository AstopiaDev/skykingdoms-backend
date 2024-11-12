package net.skykingdoms.backend.restapi.endpoints.impl;

import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.base.data.stat.StatsManager;
import net.skykingdoms.backend.base.data.stat.StatsSqlHandler;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.restapi.endpoints.ApiMethod;
import net.skykingdoms.backend.restapi.endpoints.Endpoint;
import net.skykingdoms.backend.restapi.endpoints.IEndpoint;
import net.skykingdoms.backend.restapi.endpoints.RequestType;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@ApiMethod(RequestType.GET)
@Endpoint("stats")
public final class StatsEndpoint implements IEndpoint {

    @Override
    public Object handle(final Request request,
                         final Response response) {
        MegamindApp.INSTANCE.scheduler().schedule(() -> {

        }, 0, TimeUnit.SECONDS);
        val gameTypeParam = request.queryParams("gameType");
        if (gameTypeParam == null) {
            return terminate(response, 400, "Invalid game type!");
        }

        val gameType = GameType.valueOf(gameTypeParam, null);
        if (gameType == null) {
            return terminate(response, 400, "Invalid game type!");
        }

        val gameModeParam = request.queryParams("gameMode");

        Collection<GameMode> gameModes;
        if (gameModeParam == null) {
            gameModes = new ArrayList<>(gameType.getModes());
        } else {
            val _gm = GameMode.valueOf(gameModeParam, null);
            if (_gm == null) {
                return terminate(response, 400, "Invalid game mode!");
            }

            gameModes = Collections.singleton(_gm);
        }

        val playerParam = request.queryParams("playerName");
        if (playerParam == null) {
            return terminate(response, 400, "No playerName param!");
        }

        val gamer = Gamer.getOfflineGamer(playerParam, Gamer::load);
        if (gamer == null) {
            return terminate(response, 400, "No player found!");
        }

        response.type("application/json");

        return createJson(gamer, gameModes).toString();
    }

    private JsonObject createJson(final @NonNull Gamer gamer,
                                  final @NonNull Collection<GameMode> gameModes) {
        val jsonObject = new JsonObject();
        jsonObject.addProperty("playerName", gamer.getName());
        jsonObject.addProperty("uuid", gamer.getPlayerUniqueId().toString());

        val array = new JsonObject();

        for (val gameMode : gameModes) {
            val modsJsonArray = new JsonObject();
            val statsMode = StatsManager.INSTANCE.getMode(gameMode);

            // TODO: Proper async stats loading
            val stats = StatsSqlHandler.INSTANCE.loadPlayerStats(statsMode, gamer.getGamerId(), 0).join();
            stats.forEachEntry((day, statisticsMap) -> {
                val statObject = new JsonObject();
                // statObject.addProperty("day", day);
                statisticsMap.forEachEntry((_stat, _value) -> {
                    if (_stat.getName().equalsIgnoreCase("Day")) {
                        return true;
                    }

                    statObject.addProperty(_stat.getName(), _value);
                    return true;
                });

                modsJsonArray.asMap().put(String.valueOf(day), statObject);

                return true;
            });

            array.asMap().put(gameMode.name(), modsJsonArray);
        }

        jsonObject.add("stats", array);

        return jsonObject;
    }
}
