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
@Endpoint("player")
public final class PlayerDataEndpoint implements IEndpoint {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        val playerName = request.queryParams("playerName");
        if (playerName == null) {
            return terminate(response, 400, "No playerName param found!");
        }

        val gamer = Gamer.getOfflineGamer(playerName, Gamer::load);
        if (gamer == null) {
            return terminate(response, 400, "No player with this name found");
        }

        response.status(200);
        response.type("application/json");

        return createJson(gamer).toString();
    }

    private JsonObject createJson(final @NonNull Gamer gamer) {
        val jsonObject = new JsonObject();
        jsonObject.addProperty("playerName", gamer.getName());
        jsonObject.addProperty("uuid", gamer.getPlayerUniqueId().toString());
/*        jsonObject.addProperty("primaryRank", gamer.getRanks().getPriorityRank().getGroup().name());
        jsonObject.addProperty("secondaryRank", gamer.getRanks().getPriorityRank(PermissionType.DEFAULT).getGroup().name());*/
        jsonObject.addProperty("experience", gamer.getExperience());

/*        jsonObject.addProperty("gold", gamer.getProperty(PropertyType.GOLD));*/
        jsonObject.addProperty("currentServer", gamer.getCurrentServer() == null ? "null" : gamer.getCurrentServer().getName());
/*        jsonObject.addProperty("karma", gamer.getProperty(PropertyType.KARMA));*/

        return jsonObject;
    }
}
