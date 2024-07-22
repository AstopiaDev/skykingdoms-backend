package net.skykingdoms.backend.base.data.currencies;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.protocol.server.GameType;

/**
 * @author Vihara
 */
public final class CurrenciesSqlHandler {

    public static final CurrenciesSqlHandler INSTANCE = new CurrenciesSqlHandler();

    // ----------------------------------------------------------------------------------------

    String LOAD_ALL_CURRENCIES = "SELECT * FROM `PlayerData`.`GameCurrencies` WHERE `Id` = ?;";
    String UPDATE_CURRENCIES = "INSERT INTO `PlayerData`.`GameCurrencies` " +
            "(`Id`, `sc_tokens`, `sc_coins`) " +
            "VALUES(?, ?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "`sc_tokens` = ?, `sc_coins` = ?;";

    Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    // name to amount map
    public GameCurrenciesHolder loadBalance(final int gamerId) {
        return database.executeQuery(LOAD_ALL_CURRENCIES, gamerId).thenApply(rows -> {
            val holder = new GameCurrenciesHolder(gamerId);
            if (rows.isEmpty()) {
                return holder;
            }

            val row = rows.getFirst();
            for (val gameType : GameType.VALUES) {
                if (gameType == GameType.MAIN) continue;

 /*               if (gameType == GameType.SKYCLASH) {
                    holder.setTokens(row.getInt("sc_tokens").orElse(0));
                }*/

                val name = gameType.getCode() + "_coins";
                holder.setCoins(gameType, row.getInt(name).orElse(0));
            }

            return holder;
        }).join();
    }

    public void updateBalances(final @NonNull GameCurrenciesHolder holder) {
/*        database.executeUpdate(UPDATE_CURRENCIES, holder.getGamerId(),
                holder.getTokens(), holder.getCoins(GameType.SKYCLASH),
                holder.getTokens(), holder.getCoins(GameType.SKYCLASH));*/
    }
}
