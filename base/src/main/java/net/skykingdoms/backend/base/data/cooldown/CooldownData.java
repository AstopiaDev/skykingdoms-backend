package net.skykingdoms.backend.base.data.cooldown;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CooldownData {
    private final int playerId;
    private String key;
    private long cooldown;
    private long startTime;

    public CooldownData(int playerId) {
        this.playerId = playerId;
    }
}
