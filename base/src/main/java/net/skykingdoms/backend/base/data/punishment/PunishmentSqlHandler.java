package net.skykingdoms.backend.base.data.punishment;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.punishment.PunishmentMeta;
import net.skykingdoms.api.backend.punishment.PunishmentRemoveMeta;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.backend.base.mysql.BufferedQuery;
import net.skykingdoms.backend.base.mysql.BufferedRow;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Vihara
 */
public final class PunishmentSqlHandler {

    // ----------------------------------------------------------------------------
    public static final PunishmentSqlHandler INSTANCE = new PunishmentSqlHandler();
    // ----------------------------------------------------------------------------

    /**
     * Loads all punishment metas from database
     */
    String LOAD_ALL_PUNISHMENTS = "SELECT * FROM `SysInternal`.`Punishments`;";
    String LOAD_PUNISHMENTS_ASC = "SELECT * FROM `SysInternal`.`Punishments` WHERE `Id` > ? ORDER BY Id ASC LIMIT ?;";
    String LOAD_PUNISHMENTS_DSC = "SELECT * FROM `SysInternal`.`Punishments` WHERE `Id` < ? ORDER BY Id DESC LIMIT ?;";
    String LOAD_PUNISHMENT      = "SELECT * FROM `SysInternal`.`Punishments` WHERE `Id` = ?;";
    String LOAD_PUNISHMENTS_FOR = "SELECT * FROM `SysInternal`.`Punishments` WHERE `ViolatorId` = ?;";

    /**
     * Creates a punishment record
     */
    String INSERT_PUNISHMENT    = "INSERT INTO `SysInternal`.`Punishments` " +
            "(`ViolatorId`, `ExecutorId`, `Reason`, `ExecutionDate`, `ExpirationDate`, `OriginatedServer`, `PunishmentType`, `Lifted`, `LiftMeta`) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";

    /**
     * Updates punishment metadata by its ID
     */
    String UPDATE_PUNISHMENT = "UPDATE `SysInternal`.`Punishments` SET `ExpirationDate` = ?, `Lifted` = ?, `LiftMeta` = ? WHERE `Id` = ?;";

    // --------------------------------------------------------------------------------------

    /**
     * Loads punishment lift data by its ID
     */
    String LOAD_REMOVAL_META    = "SELECT * FROM `SysInternal`.`PunishmentRemovals` WHERE `Id` = ?;";

    /**
     * Creates punishment lift data
     */
    String CREATE_REMOVAL_META  = "INSERT INTO `SysInternal`.`PunishmentRemovals` " +
            "(`RemoverId`, `Reason`, `Date`) " +
            "VALUES(?, ?, ?);";

    // --------------------------------------------------------------------------------------

    MySQL database = new MySQL(DatabaseCredentials.MAIN.withDatabase("SysInternal"));

    Cache<Integer, PunishmentRemoveMeta> REMOVE_META_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    private PunishmentMeta createPunishmentMeta(BufferedRow row) {
        return new PunishmentMeta(
                row.getRequiredInt("Id"),
                row.getString("Reason").orElse("Unspecified"),
                row.getRequiredInt("ViolatorId"),
                row.getRequiredInt("ExecutorId"),
                row.getRequiredTimestamp("ExecutionDate"),
                row.getTimestamp("ExpirationDate").orElse(null),
                row.getRequiredString("OriginatedServer"),
                PunishmentType.VALUES[row.getRequiredInt("PunishmentType")],
                row.getBoolean("Lifted"),
                row.getInt("LiftMeta").orElse(-1)
        );
    }

    Function<BufferedQuery, HashSet<PunishmentMeta>> packPunishmentMeta = (BufferedQuery rows) -> {
            val ret = new HashSet<PunishmentMeta>();
            for (val row : rows) {
                ret.add(createPunishmentMeta(row));
            }
            return ret;
    };

    /**
     * Loads all punishments
     */
    public CompletableFuture<Collection<@NotNull PunishmentMeta>> loadAllPunishments() {
        return database.executeQuery(LOAD_ALL_PUNISHMENTS).thenApply(packPunishmentMeta);
    }

    /**
     * Loads all punishments limited with cursor
     */
    public CompletableFuture<Collection<@NotNull PunishmentMeta>> loadAllPunishments(int cursor, int limit, int order) {
        return database.executeQuery(order > 0 ? LOAD_PUNISHMENTS_ASC : LOAD_PUNISHMENTS_DSC, cursor, limit).thenApply(packPunishmentMeta);
    }

    /**
     * Loads all punishments for player
     */
    public CompletableFuture<Collection<@NotNull PunishmentMeta>> loadAllPunishments(final int gamerId) {
        return database.executeQuery(LOAD_PUNISHMENTS_FOR, gamerId).thenApply(packPunishmentMeta);
    }

    public CompletableFuture<PunishmentMeta> getPunishment(final int metaId) {
        return database.executeQuery(LOAD_PUNISHMENT, metaId).thenApply(rows -> {
            if (rows.isEmpty()) {
                return null;
            }

            return createPunishmentMeta(rows.getFirst());
        });
    }

    public CompletableFuture<PunishmentRemoveMeta> getPunishmentRemoveMeta(final int metaId) {
        if (REMOVE_META_CACHE.getIfPresent(metaId) != null) {
            return CompletableFuture.completedFuture(REMOVE_META_CACHE.getIfPresent(metaId));
        }

        return database.executeQuery(LOAD_REMOVAL_META, metaId).thenApply(rows -> {
            if (rows.isEmpty()) {
                return null;
            }

            val row = rows.getFirst();

            val meta = new PunishmentRemoveMeta(
                    row.getRequiredInt("Id"),
                    row.getRequiredInt("RemoverId"),
                    row.getString("Reason").orElse("Unspecified"),
                    row.getRequiredTimestamp("Date")
            );

            REMOVE_META_CACHE.cleanUp();
            REMOVE_META_CACHE.put(metaId, meta);

            return meta;
        });
    }

    /**
     * Creates punishment record instance
     *
     * @param violatorId        id of player who we ban
     * @param executorId        id of executor (0 if console/system)
     * @param reason            reason for punishment
     * @param expiration        date of expiration (null if permanent)
     * @param originatedServer   the server the gamer is getting punished on
     * @param punishmentType    type of punishment
     *
     * @return                  PunishmentMeta instance
     */
    public PunishmentMeta createPunishmentRecord(final int violatorId,
                                                 final int executorId,
                                                 final @NonNull String reason,
                                                 final @Nullable LocalDateTime expiration,
                                                 final @NonNull String originatedServer,
                                                 final @NonNull PunishmentType punishmentType) {
        val punishmentMeta = new PunishmentMeta(
                0, reason, violatorId, executorId, LocalDateTime.now(),
                expiration, originatedServer, punishmentType, false, -1);

        // Updating punishment meta ID from database
        punishmentMeta.setMetaId(database.executeUpdate(INSERT_PUNISHMENT,
                violatorId, executorId, reason, punishmentMeta.getExecutionDate(), expiration, originatedServer, punishmentType.ordinal(), false, -1
        ).join().getGeneratedKey());

        return punishmentMeta;
    }

    /**
     * Creates punishment removal record instance
     *
     * @param removerId        id of executor (0 if console/system)
     * @param reason            reason for punishment removal
     *
     * @return                  PunishmentMeta instance
     */
    public PunishmentRemoveMeta createPunishmentLiftRecord(final int removerId,
                                                           final @Nullable String reason) {
        val punishmentMeta = new PunishmentRemoveMeta(
                0, removerId, reason, LocalDateTime.now());

        // Updating punishment meta ID from database
        punishmentMeta.setMetaId(database.executeUpdate(CREATE_REMOVAL_META,
                removerId, reason, punishmentMeta.getDate()).join().getGeneratedKey());

        return punishmentMeta;
    }

    /**
     * Updates punishment meta in database
     *
     * @param metaId        punishment meta id
     * @param expiration    expiration date
     * @param lifted        ban lift status
     */
    public void updatePunishment(final int metaId,
                                 final @Nullable LocalDateTime expiration,
                                 final boolean lifted,
                                 final int liftId) {
        database.executeUpdate(UPDATE_PUNISHMENT, expiration, lifted, liftId, metaId);
    }

}
