package net.skykingdoms.backend.base.data.sql.sql;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.base.data.sql.GameMap;
import net.skykingdoms.backend.base.mysql.BufferedRow;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.base.util.pair.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class MapSqlHandler {

    public static final MapSqlHandler INSTANCE = new MapSqlHandler();

    String LOAD_MAP_BY_NAME     = "SELECT * FROM `Maps` WHERE `Name` = ?;";
    String LIST_ALL_MAP_NAMES   = "SELECT `Name`, `HumanName`, `Modes` FROM `Maps`;";
    String INSERT_MAP           = "INSERT INTO `Maps` (`Name`, `HumanName`, `Modes`, `Data`) VALUES (?, ?, ?, ?);";

    Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("SysInternal"));

    // ---------------------------------------------------------------------------------

    /**
     * L -> system name
     * R -> human name
     */
    public List<Pair<String, String>> getMaps(final @NonNull GameMode gameMode) {
        val gameType = GameType.getLinkedGameType(gameMode);
        return database.executeQuery(LIST_ALL_MAP_NAMES).thenApply(rows -> {
            val ret = new ArrayList<Pair<String, String>>();
            for (val row : rows) {
                val name = row.getRequiredString("Name");
                if (!name.startsWith(gameType.getCode())) {
                    continue;
                }

                // map not for this mode
                if(Arrays.stream(row.getRequiredString("Modes").split(";")).noneMatch(modeName -> modeName.equalsIgnoreCase(gameMode.name()))) {
                    continue;
                }

                val human = row.getRequiredString("HumanName");

                ret.add(Pair.newImmutablePair(name, human));
            }
            return ret;
        }).join();
    }

    public CompletableFuture<GameMap> loadMap(final @NonNull String name) {
        return database.executeQuery(LOAD_MAP_BY_NAME, name).thenApply(rows -> {
            if (rows.isEmpty()) {
                return null;
            }

            return readMap(rows.getFirst());
        });
    }

    public void saveMap(final @NonNull GameMap map, String modes) {
        database.executeVoidUpdate(INSERT_MAP, map.getName(), map.getHumanName(), modes, map.getData()).join();
    }

    private GameMap readMap(@NonNull BufferedRow row) {
        return new GameMap(
                row.getRequiredString("Name"),
                row.getRequiredString("HumanName"),
                row.getRequiredBlob("Data")
        );
    }

}
