package net.skykingdoms.backend.base.data.currencies;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import java.util.concurrent.TimeUnit;

/**
 * @author Vihara
 */
public final class CurrenciesSqlHandler {

    public static final CurrenciesSqlHandler INSTANCE = new CurrenciesSqlHandler();

    // ----------------------------------------------------------------------------------------

    String LOAD_ALL_CURRENCIES = "SELECT * FROM `PlayerData`.`GameCurrencies` WHERE `Id` = ?;";
    String UPDATE_CURRENCIES = "INSERT INTO `PlayerData`.`GameCurrencies` " +
            "(`Id`, `spvp_coins`) " +
            "VALUES(?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE " +
            "`spvp_coins` = ?";

    Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));
    Cache<Integer, GameCurrenciesHolder> currencyHolders = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

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


                val name = gameType.getCode() + "_coins";
                holder.setCoins(gameType, row.getInt(name).orElse(0));
            }

            return holder;
        }).join();
    }

    public void updateBalances(final @NonNull GameCurrenciesHolder holder) {
        database.executeUpdate(UPDATE_CURRENCIES, holder.getGamerId(), holder.getCoins(GameType.SKYPVP),
                holder.getCoins(GameType.SKYPVP));
    }
}
