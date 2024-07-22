package net.skykingdoms.backend.base.data.property;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.protocol.type.PropertyType;
import net.skykingdoms.backend.protocol.util.TextFormat;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public enum PropertySqlHandler {
    INSTANCE;

    private static final String LOAD_PROPERTIES = "SELECT * FROM `PlayerData`.`Properties` WHERE `Id` = ?;";

    private static final String GET_PROPERTY_VALUE = "SELECT ? FROM `PlayerData`.`Properties` WHERE `Id` = ?;";

    private static final String WIPE_SPECIFIC_PROP = "UPDATE `PlayerData`.`Properties` SET {} = {};";

    private static final String LOAD_TOP_PLAYER_PROPERTIES = "SELECT `Id`, {} FROM `Properties` ORDER BY {} {};";


    private final Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    public CompletableFuture<TObjectIntMap<PropertyType>> loadProperties(final int playerId) {
        return database.executeQuery(LOAD_PROPERTIES, playerId).thenApply(rows -> {
            TObjectIntMap<PropertyType> properties = new TObjectIntHashMap<>();
            if (rows.isEmpty()) {
                // returning properties with default ZERO value
                Arrays.stream(PropertyType.VALUES).forEach(pt -> properties.put(pt, 0));
                return properties;
            }

            val row = rows.getFirst();
            for (val pt : PropertyType.VALUES) {
                properties.put(pt, row.getInt(pt.name()).orElse(0));
            }

            return properties;
        });
    }

    public CompletableFuture<Integer> getPropertyValue(final int playerId,
                                                       final @NonNull PropertyType property) {
        return database.executeQuery(GET_PROPERTY_VALUE, property.name()).thenApply(rows -> {
            if (!rows.isEmpty()) {
                return rows.getFirst().getRequiredInt(property.name());
            }

            return 0;
        });
    }

    // prop name, gamer id, prop name,
    // prop name, prop name, value
    public CompletableFuture<?> addPropertyValue(final int playerId,
                                                 final @NonNull PropertyType property,
                                                 final int value) {
        ///
        return database.executeVoidUpdate("INSERT INTO `Properties` (`Id`, " + property.name() + ") VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE ? = ? + ?;",
                playerId, property.name(),
                property.name(), property.name(), value);
    }

    // prop name, gamer id, prop name,
    // prop name, value
    public CompletableFuture<?> setPropertyValue(final int playerId,
                                                 final @NonNull PropertyType property,
                                                 final int value) {
        // do not ask questions. this is the solution. I hate my life
        return database.executeUpdate(
                TextFormat.formatText("INSERT INTO `PlayerData`.`Properties` (`Id`, {}) VALUES({}, {}) ON DUPLICATE KEY UPDATE {} = {};",
                        property.name(),        // 1
                        playerId,               // 2
                        value,                  // 3
                        property.name(),        // 4
                        value));                // 5
    }

    // we don't actually delete shit from database, just setting value to 0
    public CompletableFuture<?> removeProperty(final int playerId,
                                               @NonNull PropertyType property) {
        return setPropertyValue(playerId, property, 0);
    }

    public CompletableFuture<TIntIntMap> loadTopPlayerProperties(
            final @NonNull PropertyType property,
            final boolean ascending,
            final int limit
    ) {
        return database.executeQuery(TextFormat.formatText(LOAD_TOP_PLAYER_PROPERTIES, property.name(), property.name(), ascending ? "ASC" : "DESC"))
                .thenApply(rows -> {
                    TIntIntMap topPlayers = new TIntIntHashMap(limit);

                    for (val row : rows) {
                        int playerId = row.getRequiredInt("Id");

                        topPlayers.put(playerId, row.getInt(property.name()).orElse(0));

                        if (topPlayers.size() == limit) {
                            break;
                        }
                    }

                    return topPlayers;
                });
    }

    public void resetGlobalProperty(final @NonNull PropertyType propertyType,
                                    final int newValue) {
        database.executeQuery(TextFormat.formatText(WIPE_SPECIFIC_PROP,
                propertyType.name(), newValue));
    }
}
