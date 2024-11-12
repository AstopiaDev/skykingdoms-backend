package net.skykingdoms.backend.restapi.endpoints.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.val;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.permission.RankSqlHandler;
import net.skykingdoms.backend.restapi.endpoints.ApiMethod;
import net.skykingdoms.backend.restapi.endpoints.Endpoint;
import net.skykingdoms.backend.restapi.endpoints.IEndpoint;
import net.skykingdoms.backend.restapi.endpoints.RequestType;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.HashMap;

@ApiMethod(RequestType.GET)
@Endpoint("staff")
public final class StaffListEndpoint implements IEndpoint {

    @Override
    public Object handle(Request request, Response response) {
        val jsonArray = new JsonArray();

        HashMap<Integer, Group> staffRanks = new HashMap<>();

        Arrays.stream(Group.values()).filter(Group::isStaff).forEach(group -> {
            staffRanks.put(group.getId(), group);
        });

        val result = RankSqlHandler.INSTANCE.readUsernamesByRanks(staffRanks.keySet()).join();

        result.forEach((id, players) -> {
            val rankObject = new JsonObject();

            val jsonPlayers = new JsonArray();
            Arrays.stream(players).forEach(jsonPlayers::add);

            val group = staffRanks.get(id);

            rankObject.addProperty("id", id);
            rankObject.addProperty("name", group.getName());
            rankObject.addProperty("color", group.getPrefixColor());
            rankObject.add("players", jsonPlayers);

            jsonArray.add(rankObject);
        });

        response.type("application/json");

        return jsonArray.toString();
    }
}
