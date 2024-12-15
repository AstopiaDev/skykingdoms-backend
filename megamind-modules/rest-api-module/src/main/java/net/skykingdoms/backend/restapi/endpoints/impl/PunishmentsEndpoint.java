package net.skykingdoms.backend.restapi.endpoints.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.val;
import net.skykingdoms.backend.base.data.punishment.PunishmentSqlHandler;
import net.skykingdoms.backend.megamind.data.punishment.PunishmentManager;
import net.skykingdoms.backend.restapi.endpoints.*;
import spark.Request;
import spark.Response;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Sends list of registered nodes/developers
 */
@ApiMethod(RequestType.GET)
@Endpoint("punishments/:cursor")
@AuthorizedOnly
public final class PunishmentsEndpoint implements IEndpoint {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        try {
            val cursor = Integer.parseInt(request.params("cursor"));
            val punishments = PunishmentSqlHandler.INSTANCE.loadAllPunishments(cursor, 10, 1).join();

            val jsonArray = new JsonArray();

            punishments.stream().forEach(punishment -> {
                val jsonPunishment = new JsonObject();

                jsonPunishment.addProperty("violator", punishment.getViolatorId());

                jsonArray.add(jsonPunishment);
            });
        } catch (NumberFormatException e) {
            return "Invalid request made";
        } catch (Exception e) {
            return "Unknown error occurred";
        }

        return "";
    }
}