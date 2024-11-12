package net.skykingdoms.backend.base.permission;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.util.TextFormat;
import net.skykingdoms.backend.base.mysql.BufferedRow;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RankSqlHandler {

    public static RankSqlHandler INSTANCE = new RankSqlHandler();

    private static final String LOAD_PLAYER_RANKS
            = "SELECT `Id`, `GroupId`, `Expires` FROM `UserRanks` WHERE `UserId` = ?;";

    private static final String ADD_PLAYER_RANK
            = "INSERT INTO `UserRanks` (`UserId`, `GroupId`, `Expires`) VALUES (?, ?, ?)";

    private static final String REMOVE_PLAYER_RANK
            = "DELETE FROM `UserRanks` WHERE `Id` = ?";

    private static final String LOAD_PLAYERS_BY_RANK
            = "SELECT `UserId` FROM `UserRanks` WHERE `GroupId` %s ? ORDER BY `GroupId` DESC;";

    private static final String REMOVE_ALL_PLAYER_RANKS
            = "DELETE FROM `UserRanks` WHERE `UserId` = ?";

    private static final String LOAD_PLAYER_IDS_BY_RANKS
            = "SELECT GroupId, GROUP_CONCAT(DISTINCT(u.Username)) Usernames " +
            "FROM UserRanks " +
            "JOIN UserRecords u ON UserId = u.Id " +
            "WHERE GroupId IN ({}) " +
            "AND u.Username <> \"Unknown\" " +
            "GROUP BY GroupId ORDER BY GroupId DESC;";

    Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    public CompletableFuture<UserRank> addUserRank(final int userId,
                                                   final @NonNull Group group,
                                                   final @Nullable LocalDateTime expires) {
        return database.executeUpdate(ADD_PLAYER_RANK, userId, group.getId(), expires)
                .thenApply(query -> new UserRank(query.getGeneratedKey(), group, expires));
    }

    public CompletableFuture<?> removeUserRank(final int rankId) {
        return database.executeVoidUpdate(REMOVE_PLAYER_RANK, rankId);
    }

    public CompletableFuture<TIntList> getPlayersByRank(@NonNull Group group, boolean strict) {
        return database.executeQuery(String.format(LOAD_PLAYERS_BY_RANK, strict ? "=" : ">="), group.getId())
                .thenApply(query -> {
                    val playerIds = new TIntArrayList();

                    for (val row : query) {
                        playerIds.add(row.getRequiredInt("UserId"));
                    }

                    return playerIds;
                });
    }

    public CompletableFuture<UserRanks> loadUserRanks(final int playerId) {
        return database.executeQuery(LOAD_PLAYER_RANKS, playerId).thenApply(query -> {
            Set<UserRank> ranks = new HashSet<>();

            query.forEach(row -> ranks.add(readUserRank(row)));

            return new UserRanks(ranks);
        });
    }

    private UserRank readUserRank(final @NonNull BufferedRow row) {
        val group = Group.getById(null, row.getRequiredInt("GroupId"));
        val expires = row.getTimestamp("Expires").orElse(null);

        return new UserRank(row.getRequiredInt("Id"), group, expires);
    }

    public CompletableFuture<UserRank> setRank(final int userId,
                                               final @NonNull Group group,
                                               final @Nullable LocalDateTime expires) {
        return database.executeVoidUpdate(REMOVE_ALL_PLAYER_RANKS, userId)
                .thenCompose(ignored -> addUserRank(userId, group, expires));
    }

    public CompletableFuture<LinkedHashMap<Integer, String[]>> readUsernamesByRanks(final @NonNull Set<Integer> rankIds) {
        String rankIdsFormatted = String.join(", ", rankIds.stream().map(String::valueOf).toArray(String[]::new));
        String formattedQuery = TextFormat.formatText(LOAD_PLAYER_IDS_BY_RANKS, rankIdsFormatted);

        return database.executeQuery(formattedQuery).thenApply(rows -> {
            val result = new LinkedHashMap<Integer, String[]>();

            rows.forEach(row -> {
                result.put(
                        row.getRequiredInt("GroupId"),
                        row.getRequiredString("Usernames").split(",")
                );
            });

            return result;
        });
    }
}