package net.skykingdoms.backend.base.data.auth;

import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.base.util.hash.HashType;
import net.skykingdoms.backend.base.util.pair.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public enum AuthSqlHandler {
    INSTANCE;

    private static final String FETCH_PREMIUM = "SELECT `Premium` FROM `AuthRecords` WHERE `Id` = ?;";
    private static final String FETCH_AUTH_RECORD = "SELECT * FROM `AuthRecords` WHERE `Id` = ?;";
    private static final String INSERT_AUTH_RECORD = "INSERT INTO `AuthRecords` (`Id`, `Premium`, `Password`, `HashType`)" +
            " VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `Premium` = ?, `Password` = ?, `HashType` = ?;";
    private static final String REMOVE_AUTH_RECORD = "DELETE FROM `AuthRecords` WHERE `Id` = ?;";
    private static final String CHECK_EXISTENCE = "SELECT 1 FROM `AuthRecords` WHERE `Id` = ? LIMIT 1;";

    private MySQL database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    public void insertAuthRecord(int gamerId, boolean premium, @Nullable String password, @Nullable HashType hashType) {
        database.executeUpdate(INSERT_AUTH_RECORD, gamerId, premium, password, hashType == null ? null : hashType.name(), premium, password, hashType == null ? null : hashType.name()).join();
    }

    public CompletableFuture<Pair<String, HashType>> getPassword(int gamerId) {
        return database.executeQuery(FETCH_AUTH_RECORD, gamerId).thenApply(rows -> {
            if (rows.isEmpty()) {
                return null;
            }

            String password = rows.getFirst().getRequiredString("Password");
            HashType hashType = HashType.valueOf(rows.getFirst().getRequiredString("HashType"));

            return new Pair<>(password, hashType);
        });
    }

    public CompletableFuture<Boolean> isPremium(int gamerId) {
        return database.executeQuery(FETCH_PREMIUM, gamerId)
                .thenApply((rows) -> {
                    if (rows.isEmpty()) {
                        return null;
                    }

                    return rows.getFirst().getBoolean("Premium");
                });
    }

    // TODO: might need to check if password and hashtype are not null as well
    public CompletableFuture<Boolean> exists(int gamerId) {
        return database.executeQuery(CHECK_EXISTENCE, gamerId)
                .thenApply(rows -> !rows.isEmpty());
    }

    public CompletableFuture<Void> removeAuthRecord(int gamerId) {
        return database.executeUpdate(REMOVE_AUTH_RECORD, gamerId)
                .thenApply(rowsAffected -> null);
    }
}
