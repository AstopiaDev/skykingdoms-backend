package net.skykingdoms.backend.base.permission;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.luckperms.rest.LuckPermsRestClient;
import net.luckperms.rest.model.Node;
import net.luckperms.rest.model.NodeType;
import net.skykingdoms.backend.protocol.logging.Logging;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RankSqlHandler {

    public static final RankSqlHandler INSTANCE = new RankSqlHandler();

    private static final String LOAD_PLAYER_RANKS
            = "SELECT `Id`, `GroupId`, `Expires` FROM `UserRanks` WHERE `UserId` = ?;";

    private static final String ADD_PLAYER_RANK
            = "INSERT INTO `UserRanks` (`UserId`, `GroupId`, `Expires`) VALUES (?, ?, ?)";

    private static final String REMOVE_PLAYER_RANK
            = "DELETE FROM `UserRanks` WHERE `Id` = ?";

    private static final String LOAD_PLAYERS_BY_RANK
            = "SELECT `UserId` FROM `UserRanks` WHERE `GroupId` %s ? ORDER BY `GroupId` DESC;";

    // ---------------------------------------------------

    @Setter
    LuckPermsRestClient luckPerms;

/*    public CompletableFuture<UserRank> addUserRank(final int userId,
                                                   final @NonNull Group group,
                                                   final LocalDateTime expires) {
        luckPerms.users().nodesAd
        return database.executeUpdate(ADD_PLAYER_RANK, userId, group.getId(), expires)
                .thenApply(query -> new UserRank(query.getGeneratedKey(), group, expires));
    }*/

/*    public CompletableFuture<?> removeUserRank(final int rankId) {
        if (luckPerms == null) {
            return null;
        }
        return database.executeVoidUpdate(REMOVE_PLAYER_RANK, rankId);
    }*/

/*    public CompletableFuture<TIntList> getPlayersByRank(@NonNull Group group, boolean strict) {
        if (luckPerms == null) {
            return null;
        }
        return database.executeQuery(String.format(LOAD_PLAYERS_BY_RANK, strict ? "=" : ">="), group.getId())
                .thenApply(query -> {
                    val playerIds = new TIntArrayList();

                    for (val row : query) {
                        playerIds.add(row.getRequiredInt("UserId"));
                    }

                    return playerIds;
                });
    }*/

    public CompletableFuture<UserRanks> loadUserRanks(UUID uuid) {
        if (luckPerms == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                val list = luckPerms.users().nodes(uuid).execute().body().stream()
                        .filter(node -> node.key().startsWith("group."))
                        .collect(Collectors.toList());

                Set<UserRank> ranks = new HashSet<>();
                for (val node : list) {
                    ranks.add(readUserRank(node));
                }
/*                Logging.IMP.info("Loaded ranks for " + uuid + ": " + ranks.size());
                for (val rank : ranks) {
                    Logging.IMP.info("Rank: " + rank.getGroup().getName());
                }*/
                return new UserRanks(ranks);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private UserRank readUserRank(final @NonNull Node node) {
        if (luckPerms == null) {
            return null;
        }
/*
        Logging.IMP.info("Node: " + node.key());
*/
        val group = Group.fromLegacyGroup(node.key().substring(6));
        val expires = node.expiry();

        if (expires == null) {
           return new UserRank(-1, group, -1);
        }

        return new UserRank(-1, group, LocalDateTime.ofInstant(Instant.ofEpochMilli(expires), ZoneId.systemDefault()));
    }


}
