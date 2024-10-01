package net.skykingdoms.backend.base.data.stat;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.base.mysql.BufferedQuery;
import net.skykingdoms.backend.base.mysql.BufferedRow;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.base.util.pair.NumberPair;
import net.skykingdoms.backend.base.util.time.TimeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class StatsSqlHandler {

    public static final StatsSqlHandler INSTANCE = new StatsSqlHandler();

    private final Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("Stats"));

    /**
     * Получить статистику игрока на режиме
     *
     * @param mode     - режим статистики
     * @param playerId - id игрока
     * @param fromDay  - время начала (кол-во дней)
     * @return - статистика игрока
     */
    public CompletableFuture<TIntObjectMap<TObjectDoubleMap<StatsField>>> loadPlayerStats(
            @NonNull StatsMode mode, int playerId, int fromDay) {

        return database.executeQuery(mode.getSelectQuery(), playerId, fromDay).thenApply(rows -> {
            Logging.IMP.info("[DEBUG] loadPlayerStats for {}", playerId);
            TIntObjectMap<TObjectDoubleMap<StatsField>> stats = new TIntObjectHashMap<>();

            for (BufferedRow row : rows) {
                TObjectDoubleMap<StatsField> dayStats = new TObjectDoubleHashMap<>();

                for (StatsField statsField : mode.getFields()) {
/*                    Logging.IMP.info("[DEBUG] StatsField ({}) puttyyy -> {} = {}", mode.getTable(), statsField.getName(),
                            row.getInt(statsField.getName()).orElse(0));*/
                    dayStats.put(statsField, row.getDouble(statsField.getName()).orElse(0.0));
                }

                stats.put((int) dayStats.get(mode.getDay()), dayStats);
            }

            return stats;
        });
    }

    /**
     * Добавить значение к текущему в статистике
     *
     * @param field    - поле, для которого добавлять значение
     * @param playerId - id игрока
     * @param day      - день за который добавлять статистику
     * @param value    - значение, которое добавлять
     */
    public CompletableFuture<?> addPlayerStatValue(
            @NonNull StatsField field, int playerId, int day, double value) {

        val shit = field.getAddPlayerStatQuery();
        return database.executeVoidUpdate(shit,
                playerId, day, value, value);
    }

    // Tables and Fields

    /**
     * Загрузить все таблицы из базы данных статистики
     *
     * @return - список таблиц
     */
    CompletableFuture<Set<String>> loadTables() {
        return database.executeQuery("SHOW TABLES FROM `Stats`").thenApply(rows -> {
            Logging.IMP.info("[StatsHandler] StatsSqlHandler:85 -> {}", Arrays.toString(rows.stream().map(AbstractMap::toString).toArray()));
            Set<String> tables = new HashSet<>();

            for (BufferedRow row : rows) {
                val shit = row.values().stream().findFirst().orElse("NULL");
                Logging.IMP.info("[StatsHandler]  loadTables: {}", shit);
                String name = shit.toString();

                val type = GameType.valueOf(name.toUpperCase(), null);
                val mode = GameMode.valueOf(name.toUpperCase(), null);

                if (mode == null && type == null) {
                    Logging.IMP.warn("[StatsHandler] Tried loading mode {}, but enum is NULL!", name);
                    continue;
                }

                tables.add(name);
            }

            return tables;
        });
    }

    /**
     * Загрузить режим статистики по названию таблицы
     *
     * @param table - название таблицы в БД
     * @return - режим статистики
     */
    CompletableFuture<StatsMode> loadMode(@NonNull String table) {
        return database.executeQuery("SHOW FULL COLUMNS FROM `Stats`.`" + table + "`;")
                .thenApply(rows -> {
                    StatsMode mode = new StatsMode(table);

                    for (BufferedRow row : rows) {
                        //val shit = row.entrySet().stream().map(e -> e.getKey() + " - " + e.getValue().toString()).collect(Collectors.joining(", "));
                        //Logging.IMP.info("loadMode({}) -> {}", table, shit);
                        String type = row.getRequiredString("COLUMN_TYPE");
                        String name = row.getRequiredString("COLUMN_NAME");

                        if (!type.contains("int") && !type.contains("double") || name.contains("Id")) {
                            //Logging.IMP.info("[DEBUG] Continued breakpoint for {} ({})", name, type);
                            continue;
                        }

                        mode.addField(new StatsField(mode, name, row.getRequiredString("COLUMN_COMMENT")));
                    }

                    mode.buildSelectQuery();

                    return mode;
                });
    }

    public CompletableFuture<List<NumberPair>> getTemporaryTop(
            @NonNull StatsField field, int fromDay, int limit) {

        return getTemporaryTop(field, fromDay, limit, false);
    }

    /**
     * Получить топ за какой-то промежуток времени
     *
     * @param field     - поле топа
     * @param fromDay   - старт промежутка (стартовый день)
     * @param limit     - максимальное количество
     * @param ascending - режим сортировки (false = DESC, true = ASC)
     * @return - топ игроков
     */
    public CompletableFuture<List<NumberPair>> getTemporaryTop(
            @NonNull StatsField field, int fromDay, int limit, boolean ascending) {

        String function;

        switch (field.getTemporaryDisplayMode()) {
            case "max": {
                function = "MAX";
                break;
            }
            case "min": {
                function = "MIN";
                break;
            }
            default: {
                function = "SUM";
            }
        }

        return database.executeQuery(field.getTopQuery(function, ascending), fromDay)
                .thenApply(rows -> processPlayerTop(limit, rows));
    }

    private List<NumberPair> processPlayerTop(int limit, BufferedQuery rows) {
        List<NumberPair> bestPlayers = new ArrayList<>(limit);

        for (BufferedRow row : rows) {
            int playerId = row.getRequiredInt("Id");

/*
            if (PunishmentSqlHandler.INSTANCE.loadAllPunishments(playerId).join().stream()
                    .anyMatch(meta -> meta.getPunishmentType() == PunishmentType.BAN
                            && (!meta.isLifted() && !meta.isExpired()))) {
                continue;
            }
*/

            bestPlayers.add(
                    NumberPair.newImmutablePair(
                            playerId,
                            row.getRequiredInt("Total")
                    )
            );

            if (bestPlayers.size() == limit) {
                break;
            }
        }

        return bestPlayers;
    }

    public CompletableFuture<List<NumberPair>> getTopAllTime(@NonNull StatsField field, int limit) {
        return getTopAllTime(field, limit, false);
    }

    /**
     * Получить топ за все время
     *
     * @param field     - поле топа
     * @param limit     - максимальное количество
     * @param ascending - режим сортировки (false = DESC, true = ASC)
     * @return - топ игроков
     */
    public CompletableFuture<List<NumberPair>> getTopAllTime(
            @NonNull StatsField field, int limit, boolean ascending) {

        return database.executeQuery(field.getAllTimeTopQuery(ascending))
                .thenApply(rows -> processPlayerTop(limit, rows));
    }

    public void deleteStats(int playerId, @NotNull StatsMode mode) {
        deleteStats(playerId, mode, 0);
    }

    public void deleteStats(int playerId, @NotNull StatsMode mode, int fromDay) {
        if (fromDay > 0) {
            int today = (int) TimeUtil.getDayUTC();

            TIntObjectMap<TObjectDoubleMap<StatsField>> stats
                    = loadPlayerStats(mode, playerId, fromDay).join();

            TObjectDoubleMap<StatsField> overall = stats.get(0);

            if (overall != null) {
                for (StatsField field : mode.getFields()) {
                    double overallValue = overall.get(field);

                    for (int day = fromDay; day <= today; day++) {
                        TObjectDoubleMap<StatsField> daily = stats.get(day);

                        if (daily == null) {
                            continue;
                        }

                        overallValue -= daily.get(field);

                        if (overallValue < 0) {
                            overallValue = 0;
                            break;
                        }
                    }

                    database.executeVoidUpdate(String.format("UPDATE `Stats`.`%s` SET `%s` = ? WHERE `Id` = ? AND `Day` = 0", mode.getTable(), field.getName()),
                            overallValue, playerId);
                }
            }
        }

        database.executeVoidUpdate(String.format("DELETE FROM `Stats`.`%s` WHERE `Id` = ? AND `Day` >= ?", mode.getTable()), playerId, fromDay);
    }

}