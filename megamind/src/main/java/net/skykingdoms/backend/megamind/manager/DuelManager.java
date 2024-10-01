package net.skykingdoms.backend.megamind.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.manager.deployment.DeploymentResponse;
import net.skykingdoms.backend.protocol.packet.play.ArenaBoot;
import net.skykingdoms.backend.protocol.packet.play.S38UpdateDuelData;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import net.skykingdoms.backend.protocol.type.GameBootResponse;
import net.skykingdoms.backend.protocol.type.duel.FluentDuelRequestData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
public enum DuelManager {
    INSTANCE;

    final Map<Integer, DuelRequest> requests = new HashMap<>();
    @Setter
    boolean serverDeployed = false;

    // TODO: ADD A WAY OF REMOVING REQUESTS WHEN THE TARGET ACCEPTS

    public void postBootstrap() {
        AsyncExecution.INSTANCE.runTimer(() -> {
            for (DuelRequest request : requests.values()) {
                if (System.currentTimeMillis() - request.getCreatedAt() > 60000) {
                    Gamer sender = Gamer.getOfflineGamer(request.getSenderId(), ($) -> {
                        $.loadPlayerName();
                        $.loadGroups();
                    });
                    Gamer target = Gamer.getOfflineGamer(request.getTargetId(), ($) -> {
                        $.loadPlayerName();
                        $.loadGroups();
                    });

                    if (sender != null && sender.isOnline()) {
                        if (target != null) {
                            sender.sendMessage("§cYour duel request to " + target.getDisplayName() + " has expired.");
                        }
                    }

                    if (target != null && target.isOnline()) {
                        if (sender != null) {
                            target.sendMessage("§cDuel request from " + sender.getDisplayName() + " has expired.");
                        }
                    }

                    requests.remove(request.getSenderId());
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void addRequest(int senderId, int targetId, GameMode mode, FluentDuelRequestData data) {
        requests.put(senderId, new DuelRequest(System.currentTimeMillis(), senderId, targetId, mode, data));
    }

    public void removeRequest(int id) {
        requests.remove(id);
        Logging.IMP.info("[DuelManager/R] " + id + " removed");
    }

    public DuelRequest getRequest(int id) {
        if (requests.containsKey(id)) {
            return requests.get(id);
        } else {
            for (DuelRequest request : requests.values()) {
                if (request.getTargetId() == id) {
                    return request;
                }
            }
        }
        return null;
    }

    public boolean requestExists(int senderId, int targetId) {
        return requests.containsKey(senderId) && requests.get(senderId).getTargetId() == targetId;
    }

    public String getAppropriateServer(Gamer sender, GameMode gameMode, FluentDuelRequestData data) {
        if (!serverDeployed) {
            return null;
        }
        val server = ServerManager.INSTANCE.getBestGameServerByOnlineAndMap(sender, gameMode.bakeData(), data.map(), true);
        String serverName = null;

        if (server == null) {
            GameBootResponse response = new GameBootResponse(gameMode, 1);

            val duelServer = ServerManager.INSTANCE.getServerByType(GameType.DUELS, 60);
            if (duelServer != null) {
                serverName = duelServer.getChannel().awaitPacket(new ArenaBoot.Request(gameMode, data)).join().getName();
            } else {
                DeploymentManager.INSTANCE.sendDeploymentRequest(new BukkitDeployRequest(response.getGameMode().bakeData()),
                        ServerDeploy.DeploymentTarget.BUKKIT, false, null, -1,
                        (result) -> {
                            if (result == DeploymentResponse.DENIED_BY_NODE
                                    || result == DeploymentResponse.DEPLOYMENT_TIMEOUT
                                    || result == DeploymentResponse.TRANSACTION_FAILED) {
                                Logging.IMP.error("Transaction failed! (" + result.name() + ") / (#" + result.transactionId() + ")");
                            } else {
                                ServerManager.INSTANCE.queueGameInstanceBoot(result.transactionId(), new GameBootResponse[]{response});
                                Logging.IMP.info("Queued deployment of game instances!");
                            }
                        });
                serverDeployed = true;
            }
        } else {
            serverName = server.getName();
            server.getChannel().sendPacket(new S38UpdateDuelData(serverName, gameMode, data));
        }

        return serverName;
    }
}

