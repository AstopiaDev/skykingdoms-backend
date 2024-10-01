package net.skykingdoms.backend.base.data.network;

import lombok.Getter;
import net.skykingdoms.api.protocol.type.GameMode;

import java.util.UUID;

public class PersistentServerMeta {
    @Getter
    int id;
    @Getter
    String name;
    @Getter
    int port;
    @Getter
    GameMode gameMode;
    @Getter
    UUID transactionId;

    public PersistentServerMeta(int id, String name, int port, GameMode gameMode, UUID transactionId) {
        this.id = id;
        this.name = name;
        this.port = port;
        this.gameMode = gameMode;
        this.transactionId = transactionId;
    }
}
