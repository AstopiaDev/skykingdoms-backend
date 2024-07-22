package net.skykingdoms.backend.base.data.skin.sql;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.data.skin.Skin;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public enum SkinsSqlHandler {

    INSTANCE;

    private static final String GET_SKIN_DATA = "SELECT * FROM `SysInternal`.`SkinsStorage` WHERE `Name` = ?;";
    private static final String ADD_SKIN_DATA = "INSERT INTO `SysInternal`.`SkinsStorage` (`Name`, `Uuid`, `Value`, `Signature`, `Timestamp`) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `Uuid` = ?, `Value`= ?, `Signature`= ?, `Timestamp` = ?;";
    private static final String FLUSH_SKIN_DATA = "DELETE FROM `SysInternal`.`SkinsStorage` WHERE `Name` = ?;";
    private static final String GET_RANDOM_SKINS_DATA = "SELECT * FROM `SysInternal`.`SkinsStorage` ORDER BY RAND() LIMIT ?;";

    private final Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("SysInternal"));

    public Skin getSkinByName(final @NonNull String name) {
        return database.executeQuery(GET_SKIN_DATA, name).thenApply(resultSet -> {
            if (resultSet.isEmpty()) {
                return null;
            }

            val skin = resultSet.getFirst();
            val zonedTime = skin.getRequiredTimestamp("Timestamp").atZone(ZoneId.systemDefault());
            return new Skin(name,
                    skin.getRequiredString("Uuid"),
                    skin.getRequiredString("Value"),
                    skin.getRequiredString("Signature"),
                    zonedTime.toInstant().toEpochMilli());
        }).join();
    }

    public void addSkin(final @NonNull Skin skin) {
        val timestamp = new Timestamp(skin.getTimestamp());
        database.executeVoidUpdate(ADD_SKIN_DATA,
                skin.getName(), skin.getUuid(), skin.getValue(), skin.getSignature(), timestamp,
                skin.getUuid(), skin.getValue(), skin.getSignature(), timestamp);
    }

    public void flushSkinData(final @NonNull String skinName) {
        database.executeVoidUpdate(FLUSH_SKIN_DATA, skinName);
    }

    public CompletableFuture<List<Skin>> getRandomSkins(final int amount) {
        return database.executeQuery(GET_RANDOM_SKINS_DATA, amount).thenApply(resultSet -> {
            List<Skin> skins = new ArrayList<>(amount);
            while (resultSet.iterator().hasNext()) {
                val next = resultSet.iterator().next();
                val zonedTime = next.getRequiredTimestamp("Timestamp").atZone(ZoneId.systemDefault());
                skins.add(new Skin(next.getRequiredString("Name"),
                        next.getRequiredString("Uuid"),
                        next.getRequiredString("Value"),
                        next.getRequiredString("Signature"),
                        zonedTime.toInstant().toEpochMilli()));
            }
            return skins;
        });
    }
}
