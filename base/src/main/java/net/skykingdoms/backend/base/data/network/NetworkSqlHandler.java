package net.skykingdoms.backend.base.data.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Basically a database handler, which stores primary player data
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("FieldCanBeLocal")
public final class NetworkSqlHandler {

    // ----------------------------------------------------------------------
    public static final NetworkSqlHandler INSTANCE = new NetworkSqlHandler();
    // ----------------------------------------------------------------------

    /** Loads player's UUID by its GAMER ID. `Username` field is requested for caching purposes*/
    String LOAD_UUID_BY_ID = "SELECT `UUID`, `Username` FROM `PlayerData`.`UserRecords` WHERE `Id` = ?;";

    /** Loads player's UUID by its username. `Id` field is requested for caching purposes */
    String LOAD_UUID_BY_USERNAME = "SELECT `UUID`, `Id` FROM `PlayerData`.`UserRecords` WHERE `Username` = ?;";

    /** Loads Gamer ID by its username. `UUID` field is requested for caching purposes */
    String LOAD_ID_BY_NAME = "SELECT `Id`, `UUID` FROM `PlayerData`.`UserRecords` WHERE `Username` = ?;";

    /** Loads Gamer ID by its username. `Name` field is requested for caching purposes */
    String LOAD_ID_BY_UUID = "SELECT `Id`, `Username` FROM `PlayerData`.`UserRecords` WHERE `UUID` = ?;";

    /** Loads username by Gamer ID. `UUID` field is requested for caching purposes */
    String LOAD_NAME_BY_ID = "SELECT `Username`, `UUID` FROM `PlayerData`.`UserRecords` WHERE `Id` = ?;";

    /** Loads language id by Gamer ID */
    String LOAD_LANG_BY_ID = "SELECT `Language` FROM `PlayerData`.`UserRecords` WHERE `Id` = ?;";

    /** Updates language of player in database by Gamer ID */
    String UPDATE_LANG_BY_ID = "UPDATE `PlayerData`.`UserRecords` SET `Language` = ? WHERE `Id` = ?;";

    String CREATE_USER_RECORD = "INSERT INTO `PlayerData`.`UserRecords` " +
            "(`UUID`, `Username`, `Language`, `RegistrationIp`, `RegistrationDate`) VALUES (?, ?, ?, ?, ?);";

    /** In case if player changed minecraft account's name */
    String UPDATE_USERNAME = "UPDATE `PlayerData`.`UserRecords` SET `Username` = ? WHERE `Id` = ?;";

    Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    // ------------------------------------------------------------------------

    /** <UUID - Gamer ID> cache */
    Cache<UUID, CachedUserData> nameDataCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    // ------------------------------------------------------------------------

    /**
     * Loads UUID by username and caches the record
     */
    public UUID getUniqueId(final @NonNull String username) {
        val cacheAsMap = nameDataCache.asMap();
        for (val cachedRecord : cacheAsMap.entrySet()) {
            if (cachedRecord.getValue().getPlayerName().equals(username)) return cachedRecord.getKey();
        }

        return database.executeQuery(LOAD_UUID_BY_USERNAME, username).thenApply(rows -> {
            if (rows.isEmpty()) return null;

            return UUID.fromString(rows.getFirst().getRequiredString("UUID"));
        }).join();
    }

    public int createUserRecord(final @NonNull UUID playerUniqueId,
                                final @NonNull String playerName,
                                final @NonNull Language language,
                                final @NonNull InetAddress address) {
        return database.executeUpdate(
                CREATE_USER_RECORD,
                playerUniqueId.toString(),
                playerName,
                language.getId(),
                address.getHostAddress(),
                Timestamp.from(Instant.now())
        ).join().getGeneratedKey();
    }

    /**
     * Loads UUID by username and caches the record
     */
    public UUID getUniqueId(final int gamerId) {
        val cacheAsMap = nameDataCache.asMap();
        for (val cachedRecord : cacheAsMap.entrySet()) {
            if (cachedRecord.getValue().getPlayerId() == gamerId) return cachedRecord.getKey();
        }

        return database.executeQuery(LOAD_UUID_BY_ID, gamerId).thenApply(rows -> {
            if (rows.isEmpty()) return null;

            val uuid = UUID.fromString(rows.getFirst().getRequiredString("UUID"));
            val playerName = rows.getFirst().getRequiredString("Username");

            cachePlayerData(playerName, gamerId, uuid);

            return uuid;
        }).join();
    }

    /**
     * Loads username by ID and caches the record
     */
    public String getPlayerName(final int gamerId) {

        // Trying to load cached record, if present
        val cacheAsMap = nameDataCache.asMap();
        for (val cachedRecord : cacheAsMap.entrySet()) {
            if (cachedRecord.getValue().getPlayerId() == gamerId) {
                return cachedRecord.getValue().getPlayerName();
            }
        }

        return database.executeQuery(LOAD_NAME_BY_ID, gamerId).thenApply(rows -> {
            if (rows.isEmpty()) return null;

            val playerName = rows.getFirst().getRequiredString("Username");
            val uuid = UUID.fromString(rows.getFirst().getRequiredString("UUID"));

            // Caching for future requests
            cachePlayerData(playerName, gamerId, uuid);

            return playerName;
        }).join();
    }

    public int getPlayerId(final @NonNull String username) {
        return database.executeQuery(LOAD_ID_BY_NAME, username).thenApply(rows -> {
            if (rows.isEmpty()) return -1;

            val gamerId = rows.getFirst().getRequiredInt("Id");
            val uuid = UUID.fromString(rows.getFirst().getRequiredString("UUID"));

            // Caching for future requests
            cachePlayerData(username, gamerId, uuid);

            return gamerId;
        }).join();
    }

    public int getPlayerId(final @NonNull UUID uuid) {
        return database.executeQuery(LOAD_ID_BY_UUID, uuid.toString()).thenApply(rows -> {
            if (rows.isEmpty()) return -1;

            val gamerId = rows.getFirst().getRequiredInt("Id");
            val username = rows.getFirst().getRequiredString("Username");

            // Caching for future requests
            cachePlayerData(username, gamerId, uuid);

            return gamerId;
        }).join();
    }

    public void updatePlayerLanguage(final int gamerId,
                                     final Language language) {
        database.executeVoidUpdate(UPDATE_LANG_BY_ID, language.getId(), gamerId);
    }

    public void updatePlayerName(final int gamerId,
                                 final @NonNull String userName) {
        database.executeUpdate(UPDATE_USERNAME, userName, gamerId);
    }

    public Language getPlayerLanguage(final int gamerId) {
        return database.executeQuery(LOAD_LANG_BY_ID, gamerId).thenApply(rows -> {
            if (rows.isEmpty()) return Language.DEFAULT;

            return Language.getById(rows.getFirst().getRequiredInt("Language"));
        }).join();
    }

    public void cachePlayerData(final @NonNull String playerName,
                                final int playerId,
                                final UUID playerUuid) {
        nameDataCache.cleanUp();
        nameDataCache.put(playerUuid, new CachedUserData(playerId, playerName));
    }

    public void removeFromCache(final @NonNull UUID uuid) {
        nameDataCache.cleanUp();
        nameDataCache.invalidate(uuid);
    }

    /**
     * Basically a record class that contains some primary gamer fields that
     * should be persistent.
     */
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @EqualsAndHashCode
    @Data
    private static final class CachedUserData {

        int playerId;

        String playerName;

    }
}
