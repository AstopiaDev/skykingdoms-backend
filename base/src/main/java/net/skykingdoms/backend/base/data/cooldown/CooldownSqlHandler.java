package net.skykingdoms.backend.base.data.cooldown;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public enum CooldownSqlHandler {
    INSTANCE;

    private static final String LOAD_COOLDOWN = "SELECT * FROM `PlayerData`.`Cooldowns` WHERE `Id` = ?;";
    private static final String INSERT_COOLDOWN = "INSERT INTO `PlayerData`.`Cooldowns` " +
            "(`Id`, `Key`, `Cooldown`, `StartTime`) " +
            "VALUES(?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "`Cooldown` = ?;";
    private final Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));
    private final Cache<Integer, Map<String, CooldownData>> cooldownCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public Map<String, CooldownData> loadCooldown(final int playerId) throws ExecutionException {
        return cooldownCache.get(playerId, () -> database.executeQuery(LOAD_COOLDOWN, playerId).thenApply(rows -> {
            Map<String, CooldownData> cooldownDataMap = new HashMap<>();
            rows.forEach(row -> {
                val cooldownData = new CooldownData(playerId);
                cooldownData.setKey(row.getString("Key").orElse(null));
                cooldownData.setCooldown(row.getLong("Cooldown").orElse(0L));
                cooldownData.setStartTime(row.getLong("StartTime").orElse(0L));
                cooldownDataMap.put(cooldownData.getKey(), cooldownData);
            });
            return cooldownDataMap;
        }).join());
    }

    public void updateCooldown(final @NonNull CooldownData cooldownData) {
        database.executeUpdate(INSERT_COOLDOWN, cooldownData.getPlayerId(), cooldownData.getKey(),
                cooldownData.getCooldown(), cooldownData.getStartTime(), cooldownData.getCooldown());
        try {
            Map<String, CooldownData> cooldownDataMap = loadCooldown(cooldownData.getPlayerId());
            cooldownDataMap.put(cooldownData.getKey(), cooldownData);
            cooldownCache.put(cooldownData.getPlayerId(), cooldownDataMap);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void invalidateCache(final int playerId) {
        cooldownCache.invalidate(playerId);
    }

    public boolean isOnCooldown(final int playerId, final String key) {
        try {
            Map<String, CooldownData> cooldownDataMap = loadCooldown(playerId);
            CooldownData cooldownData = cooldownDataMap.get(key);
            if (cooldownData == null) {
                return false;
            }
            long currentTime = System.currentTimeMillis();
            return currentTime < cooldownData.getStartTime() + cooldownData.getCooldown();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getRemainingCooldown(final int playerId, final String key) {
        try {
            Map<String, CooldownData> cooldownDataMap = loadCooldown(playerId);
            CooldownData cooldownData = cooldownDataMap.get(key);
            if (cooldownData == null) {
                return 0;
            }
            long currentTime = System.currentTimeMillis();
            long remainingTime = cooldownData.getStartTime() + cooldownData.getCooldown() - currentTime;
            return Math.max(remainingTime, 0);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }
}