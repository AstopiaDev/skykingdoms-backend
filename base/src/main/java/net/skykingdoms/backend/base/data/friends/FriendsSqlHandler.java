package net.skykingdoms.backend.base.data.friends;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import javax.annotation.Syntax;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Vihara
 */
public final class FriendsSqlHandler {

    // ----------------------------------------------------------------------
    public static final FriendsSqlHandler INSTANCE = new FriendsSqlHandler();
    // ----------------------------------------------------------------------

    /**
     * Inserts into table array of metadata ids
     */
    String ADD_FRIEND = "INSERT INTO `PlayerData`.`Friends` (`Id`, `Metadata`) VALUES(?, ?) " +
            "ON DUPLICATE KEY UPDATE `Metadata` = ?;";

    /**
     * Creates friendship metadata
     */
    String CREATE_METADATA = "INSERT INTO `SysInternal`.`FriendshipMeta` (`FirstId`, `SecondId`, `Beginning`)" +
            "VALUES (?, ?, ?);";

    @Syntax("SQL")
    String UPDATE_METADATA = "UPDATE `SysInternal`.`FriendshipMeta` SET `FirstId` = ?, `SecondId` = ?, `Beginning` = ? WHERE `Id` = ?;";

    /**
     * Gets all friendship metadatas by gamer id
     */
    String GET_METADATAS_FOR_PLAYER = "SELECT * FROM `SysInternal`.`FriendshipMeta` WHERE `FirstId` = ? OR `SecondId` = ?;";

    /**
     * Gets all friendship metadatas by gamer id
     */
    String GET_METADATAS = "SELECT * FROM `SysInternal`.`FriendshipMeta`;";

    /**
     * Deletes friendship metadata
     */
    String DELETE_METADATA = "DELETE FROM `SysInternal`.`FriendshipMeta` WHERE `Id` = ?;";

    MySQL database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    public Collection<FriendshipMeta> getFriendships() {
        return database.executeQuery(GET_METADATAS).thenApply(rows -> {
            val ret = new HashSet<FriendshipMeta>();
            if (rows.isEmpty()) {
                return ret;
            }

            for (val row : rows) {
                ret.add(new FriendshipMeta(
                        row.getRequiredInt("Id"),
                        row.getRequiredInt("FirstId"),
                        row.getRequiredInt("SecondId"),
                        row.getTimestamp("Beginning").orElse(null)));
            }

            return ret;
        }).join();

    }

    public Set<FriendshipMeta> getFriendships(final int gamerId) {
        return database.executeQuery(GET_METADATAS_FOR_PLAYER, gamerId, gamerId).thenApply(rows -> {
            val ret = new HashSet<FriendshipMeta>();
            if (rows.isEmpty()) {
                return ret;
            }

            for (val row : rows) {
                // unaccepted friend request. we skip this
                if (!row.getTimestamp("Beginning").isPresent()) {
                    continue;
                }

                ret.add(new FriendshipMeta(
                        row.getRequiredInt("Id"),
                        row.getRequiredInt("FirstId"),
                        row.getRequiredInt("SecondId"),
                        row.getRequiredTimestamp("Beginning")));
            }

            return ret;
        }).join();
    }

    public void updateFriends(final int gamerId,
                              final int[] friendIds) {
        val friendsString = "[" + Arrays.stream(friendIds).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + "]";
        database.executeUpdate(ADD_FRIEND, gamerId, friendsString, friendsString);
    }

    /**
     * Creates FriendshipMeta object and adds it to database
     */
    public FriendshipMeta createFriendshipMeta(final int senderId,
                                               final int targetId) {
        return new FriendshipMeta(
                database.executeUpdate(CREATE_METADATA, senderId, targetId, null).join().getGeneratedKey(),
                senderId,
                targetId,
                null);
    }

    public void removeFriendshipMeta(final @NonNull FriendshipMeta meta) {
        database.executeUpdate(DELETE_METADATA, meta.getMetaId());
    }

    public void updateFriendshipMeta(final @NonNull FriendshipMeta meta) {
        database.executeUpdate(UPDATE_METADATA, meta.getFirstGamerId(), meta.getSecondGamerId(), meta.getBeginning(), meta.getMetaId());
    }
}
