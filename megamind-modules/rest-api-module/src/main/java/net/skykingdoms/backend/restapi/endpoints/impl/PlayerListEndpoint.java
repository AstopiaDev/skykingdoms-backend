package net.skykingdoms.backend.restapi.endpoints.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.val;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.restapi.endpoints.ApiMethod;
import net.skykingdoms.backend.restapi.endpoints.Endpoint;
import net.skykingdoms.backend.restapi.endpoints.IEndpoint;
import net.skykingdoms.backend.restapi.endpoints.RequestType;
import spark.Request;
import spark.Response;

@ApiMethod(RequestType.GET)
@Endpoint("list")
public final class PlayerListEndpoint implements IEndpoint {

    @Override
    public Object handle(Request request, Response response) {
        val jsonArray = new JsonArray();
        Gamer.getGamers().forEach((gamer) -> {
            val playerObject = new JsonObject();

            val rank = gamer.getRanks().getPriorityRank();

            playerObject.addProperty("name", gamer.getName());
            playerObject.addProperty("rank", rank.getDisplayName());
            playerObject.addProperty("color", rank.getGroup().getPrefixColor());

            jsonArray.add(playerObject);
        });

        response.type("application/json");

        return jsonArray.toString();
    }
}
