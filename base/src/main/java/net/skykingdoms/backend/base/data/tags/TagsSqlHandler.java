package net.skykingdoms.backend.base.data.tags;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Getter
public enum TagsSqlHandler {
    INSTANCE;

    String LOAD_ALL_TAGS = "SELECT * FROM `SysInternal`.`Tags`";
    String LOAD_ALL_TAGS_FROM_ID = "SELECT * FROM `PlayerData`.`Tags` WHERE `Uuid` = ?";
    String ADD_TAG = "INSERT INTO `SysInternal`.`Tags` (`Name`, `Season`, `Year`, `NameId`) VALUES (?, ?, ?, ?)";
    String GRAB_LUCK_TAGS = "SELECT * FROM `luckperms`.`luckperms_user_permissions`;";
    String ADD_PLAYER_TAG = "INSERT INTO `PlayerData`.`Tags` (`Uuid`, `TagId`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `TagId` = ?;";
    String REMOVE_PLAYER_TAG = "DELETE FROM `PlayerData`.`Tags` WHERE `Uuid` = ? AND `TagId` = ?";

    Database databasePlayer = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));
    Database databaseSys = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));
    Database luckPerms = new MySQL(DatabaseCredentials.MAIN.withDatabase("luckperms"));

    Int2ObjectMap<Tag> tags = new Int2ObjectOpenHashMap<>();
    Cache<UUID, HashSet<Integer>> playerTags = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public void loadAllTags() {
        if (tags.isEmpty()) {
            databaseSys.executeQuery(LOAD_ALL_TAGS).thenAccept(rows -> {
                for (val row : rows) {
                    String name = row.getString("Season").orElse(null);
                    String year = row.getString("Year").orElse(null);
                    SeasonObtained season = name == null ? null : SeasonObtained.fromString(name);
                    YearObtained yearObtained = year == null ? null : YearObtained.valueOf(year);
                    tags.put(
                            row.getRequiredInt("Id"),
                            new Tag(
                                    row.getRequiredInt("Id"),
                                    row.getRequiredString("Name"),
                                    season,
                                    yearObtained
                            )
                    );
                }
            }).join();
        }
    }

    public int getTagId(String nameId) {
        return databaseSys.executeQuery("SELECT * FROM `SysInternal`.`Tags` WHERE `NameId` = ?", nameId)
                .thenApply(rows -> {
                    val row = rows.getFirst();
                    return row.getRequiredInt("Id");
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    // Return a default value or rethrow the exception as a runtime exception
                    return -1; // Assuming -1 is a suitable default value for your use case
                })
                .join();
    }

    public void loadTags(UUID playerId) {
        Set<Integer> tags = playerTags.getIfPresent(playerId);
        if (tags == null || tags.isEmpty()) {
            databasePlayer.executeQuery(LOAD_ALL_TAGS_FROM_ID, playerId.toString()).thenAccept(rows -> {
                HashSet<Integer> playerTags = new HashSet<>();
                for (val row : rows) {
                    playerTags.add(row.getRequiredInt("TagId"));
                }
                this.playerTags.put(playerId, playerTags);
            }).join();
        }
    }

    public void addTag(String name, @Nullable SeasonObtained season, @Nullable YearObtained yearObtained, String nameId) {
        String seasonName = season == null ? null : season.name();
        String year = yearObtained == null ? null : yearObtained.name();

        databaseSys.executeUpdate(ADD_TAG, name, seasonName, year, nameId).join();
    }

    public void addPlayerTag(UUID playerId, int tagId) {
        databasePlayer.executeUpdate(ADD_PLAYER_TAG, playerId.toString(), tagId, tagId).join();

        HashSet<Integer> tags = playerTags.getIfPresent(playerId);
        if (tags == null) {
            tags = new HashSet<>();
        }
        tags.add(tagId);

        playerTags.put(playerId, tags);
    }


    public void removePlayerTag(UUID playerId, int tagId) {
        // Remove tag from the database
        databasePlayer.executeUpdate(REMOVE_PLAYER_TAG, playerId.toString(), tagId).join();

        // Update the cache
        HashSet<Integer> tags = playerTags.getIfPresent(playerId);
        if (tags != null) {
            tags.remove(tagId);
            if (tags.isEmpty()) {
                playerTags.invalidate(playerId);
            } else {
                playerTags.put(playerId, tags);
            }
        }
    }

    public Tag getTag(int tagId) {
        if (tags.isEmpty()) {
            loadAllTags();
        }

        return tags.get(tagId);
    }

    public Set<Integer> getPlayerTags(UUID playerId) {
        if (playerTags.getIfPresent(playerId) == null) {
            loadTags(playerId);
        }

        return playerTags.getIfPresent(playerId);
    }
}
