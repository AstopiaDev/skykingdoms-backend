package net.skykingdoms.backend.megamind.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.server.GameMode;
import net.skykingdoms.backend.protocol.type.duel.FluentDuelRequestData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Getter
public enum DuelManager {
    INSTANCE;

    Map<Integer, DuelRequest> requests = new HashMap<>();

    // TODO: ADD A WAY OF REMOVING REQUESTS WHEN THE TARGET ACCEPTS

    public void postBootstrap() {
        AsyncExecution.INSTANCE.runTimer(() -> {
            for (DuelRequest request : requests.values()) {
                if (System.currentTimeMillis() - request.getCreatedAt() > 30000) {
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
}

