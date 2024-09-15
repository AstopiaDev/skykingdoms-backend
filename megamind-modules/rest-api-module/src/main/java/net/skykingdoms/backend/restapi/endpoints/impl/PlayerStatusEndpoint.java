package net.skykingdoms.backend.restapi.endpoints.impl;

import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.restapi.endpoints.ApiMethod;
import net.skykingdoms.backend.restapi.endpoints.Endpoint;
import net.skykingdoms.backend.restapi.endpoints.IEndpoint;
import net.skykingdoms.backend.restapi.endpoints.RequestType;
import spark.Request;
import spark.Response;

@ApiMethod(RequestType.GET)
@Endpoint("status")
public final class PlayerStatusEndpoint implements IEndpoint {

    @Override
    public Object handle(final Request request, final Response response) {
        val playerName = request.queryParams("playerName");
        if (playerName == null) {
            return terminate(response, 400, "No playerName param!");
        }

        val gamer = Gamer.getOfflineGamer(playerName, Gamer::loadPlayerName);
        if (gamer == null) {
            return terminate(response, 400, "No playerName param!");
        }

        response.type("application/json");

        return createJson(gamer).toString();
    }

    private JsonObject createJson(final @NonNull Gamer gamer) {
        val jsonObject = new JsonObject();
        jsonObject.addProperty("playerName", gamer.getName());
        jsonObject.addProperty("uuid", gamer.getPlayerUniqueId().toString());

        val sessionObject = new JsonObject();
        sessionObject.addProperty("online", gamer.isOnline());

        val currentServer = gamer.getGameServer();
        sessionObject.addProperty("gameType", currentServer == null ? "null" : currentServer.getGameType().name());
        sessionObject.addProperty("gameMode", currentServer == null ? "null" : currentServer.getGameMode().name());
        sessionObject.addProperty("map", currentServer == null ? "null" : currentServer.getMap());

        jsonObject.add("session", sessionObject);
        return jsonObject;
    }
}
