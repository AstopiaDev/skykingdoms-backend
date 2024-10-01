package net.skykingdoms.backend.base.data.network;


import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.val;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Vihara
 */
public final class ServersSqlHandler {

    public static final ServersSqlHandler INSTANCE = new ServersSqlHandler();

    /**
     * Loads all report records from MySQL
     */
    String LOAD_ALL_SERVERS = "SELECT * FROM `SysInternal`.`PersistentServers`;";


    /**
     * Id           = id of the server
     * Name     = name of the server
     * <p>
     * Returns generated ID blya
     */
    String CREATE_SERVER = "INSERT INTO `SysInternal`.`PersistentServers` " +
            "(`GameMode`, `TransactionId`, `Port`) VALUES " +
            "(?, ?, ?);";

    String UPDATE_SERVER_NAME = "UPDATE `SysInternal`.`PersistentServers` SET `Name` = ? WHERE `Id` = ?;";

    Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("SysInternal"));

    public CompletableFuture<TIntObjectMap<PersistentServerMeta>> loadServers() {
        return database.executeQuery(LOAD_ALL_SERVERS).thenApply(rows -> {
            TIntObjectMap<PersistentServerMeta> metas = new TIntObjectHashMap<>();

            for (val row : rows) {
                int id = row.getInt("Id").getAsInt();

                metas.put(id, new PersistentServerMeta(
                        id,
                        row.getString("Name").get(),
                        row.getInt("Port").getAsInt(),
                        GameMode.valueOf(row.getString("GameMode").orElse(null)),
                        UUID.fromString(row.getString("TransactionId").orElse(null))
                ));
            }

            return metas;
        });
    }

    public void createServer(int port, final GameMode gameMode, UUID transactionId) {
        int id = database.executeUpdate(CREATE_SERVER,
                gameMode.name(),
                transactionId.toString(),
                port
        ).join().getGeneratedKey();
        database.executeUpdate(UPDATE_SERVER_NAME,
                "P" + id,
                id
        ).join();
    }
}

