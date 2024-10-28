package net.skykingdoms.backend.restapi.endpoints.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.impl.BukkitServer;
import net.skykingdoms.backend.megamind.server.impl.PersistentServer;
import net.skykingdoms.backend.restapi.endpoints.*;
import spark.Request;
import spark.Response;

import java.util.concurrent.TimeUnit;

@ApiMethod(RequestType.POST)
@Endpoint("reconnect")
@AuthorizedOnly
public class ReconnectEndpoint implements IEndpoint {
    Cache<Gamer, String> reconnectingPlayers = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build();

    public ReconnectEndpoint() {
        AsyncExecution.INSTANCE.runTimer(() -> {
            reconnectingPlayers.cleanUp();
            reconnectingPlayers.asMap().forEach((gamer, serverName) -> {
                gamer.sendMessage(ChatColor.GREEN + "Attempting to reconnect you to " + serverName + "...");
                PersistentServer server = ServerManager.INSTANCE.getPersistentServer(serverName);

                if (server == null) {
                    return;
                }

                if (server.canJoin()) {
                    gamer.sendTo(server);
                    reconnectingPlayers.invalidate(gamer);
                    gamer.sendMessage(ChatColor.GREEN + "Reconnected successfully!");
                }
            });
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        val token = request.queryParams("token");
        if (token == null) {
            return terminate(response, 400, "No token param found!");
        }

        if (!token.equals("dolbaeb-is-cooking")) {
            return terminate(response, 400, "Invalid token!");
        }

        val playerName = request.queryParams("playerName");
        if (playerName == null) {
            return terminate(response, 400, "No playerName param found!");
        }

        Gamer gamer = Gamer.getGamer(playerName);
        if (gamer == null) {
            return terminate(response, 400, "No player with this name found");
        }

        val serverName = request.queryParams("serverName");
        if (serverName == null) {
            return terminate(response, 400, "No serverName param found!");
        }

        AsyncExecution.INSTANCE.runLater(() -> {
            PersistentServer server = ServerManager.INSTANCE.getPersistentServer(serverName);
            if (server == null) {
                reconnectingPlayers.put(gamer, serverName);
            } else if (server.canJoin()) {
                gamer.sendTo(server);
                reconnectingPlayers.invalidate(gamer);
                gamer.sendMessage(ChatColor.GREEN + "Reconnected successfully!");
            }
        }, 2, TimeUnit.SECONDS);

        response.status(200);
        return "Started reconnecting successfully.";
    }
}
