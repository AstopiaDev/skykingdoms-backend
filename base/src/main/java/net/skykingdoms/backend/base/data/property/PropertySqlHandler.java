package net.skykingdoms.backend.base.data.property;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.type.property.PropertyType;
import net.skykingdoms.api.protocol.util.TextFormat;
import net.skykingdoms.backend.base.data.property.impl.IntPropertyType;
import net.skykingdoms.backend.base.data.property.impl.LongPropertyType;
import net.skykingdoms.backend.base.data.property.impl.StringPropertyType;
import net.skykingdoms.backend.base.mysql.BufferedRow;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public enum PropertySqlHandler {
    INSTANCE;

    private static final Map<Class<?>, Function<Object, PropertyType<?>>> propertyTypeMap = new HashMap<>();
    private static final Map<String, Supplier<PropertyType<?>>> propertyMap = new HashMap<>();

    private static final String LOAD_PROPERTIES = "SELECT * FROM `PlayerData`.`NewProperties` WHERE `GamerId` = ?;";

    private static final String GET_PROPERTY_VALUE = "SELECT ? FROM `PlayerData`.`NewProperties` WHERE `GamerId` = ? AND 'Key' = ?;";

    private static final String WIPE_SPECIFIC_PROP = "UPDATE `PlayerData`.`NewProperties` SET {} = {};";

    private static final String LOAD_TOP_PLAYER_PROPERTIES = "SELECT `GamerId`, {} FROM `NewProperties` ORDER BY {} {};";


    private final Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    static {
        propertyTypeMap.put(String.class, value -> new StringPropertyType());
        propertyTypeMap.put(Integer.class, value -> new IntPropertyType());
        propertyTypeMap.put(Long.class, value -> new LongPropertyType());

        propertyMap.put("STRING", StringPropertyType::new);
        propertyMap.put("INT", IntPropertyType::new);
        propertyMap.put("LONG", LongPropertyType::new);
    }

    public CompletableFuture<Object2ObjectMap<String, PropertyType<?>>> loadProperties(final int playerId) {
        return database.executeQuery(LOAD_PROPERTIES, playerId).thenApply(rows -> {
            Object2ObjectMap<String, PropertyType<?>> properties = new Object2ObjectArrayMap<>();
            if (rows.isEmpty()) {
                return properties;
            }

            rows.forEach((row) -> {
                properties.put(
                        row.getRequiredString("Name"),
                        newProperty(
                                row.getRequiredString("Type"),
                                row.getRequiredString("Data")

                        )
                );
            });

            return properties;
        });
    }

    public PropertyType<?> newProperty(String type, String data) {
        Supplier<PropertyType<?>> supplier = propertyMap.get(type);
        if (supplier != null) {
            PropertyType<?> property = supplier.get();
            property.deserialize(data);
            return property;
        }
        return null;
    }

    public <T> PropertyType<T> createProperty(T value) {
        @SuppressWarnings("unchecked")
        Function<Object, PropertyType<?>> creator = propertyTypeMap.get(value.getClass());

        if (creator != null) {
            return (PropertyType<T>) creator.apply(value);
        }

        return null;
    }

    public <T> CompletableFuture<T> getPropertyValue(final int playerId,
                                                       final @NonNull String key) {
        return database.executeQuery(GET_PROPERTY_VALUE, playerId, key).thenApply(rows -> {
            if (!rows.isEmpty()) {
                BufferedRow row = rows.getFirst();
                return (T) newProperty(
                        row.getRequiredString("Type"),
                        row.getRequiredString("Data")
                ).getValue();
            }

            return null;
        });
    }

    // prop name, gamer id, prop name,
    // prop name, value
    public CompletableFuture<?> setPropertyValue(final int playerId,
                                                 final String key,
                                                 final @NonNull PropertyType<?> property) {
        // do not ask questions. this is the solution. I hate my life
        String value = property.serialize();

        return database.executeUpdate(
                TextFormat.formatText("INSERT INTO `PlayerData`.`Properties` (`Id`, {}) VALUES({}, {}) ON DUPLICATE KEY UPDATE {} = {};",
                        key,        // 1
                        playerId,               // 2
                        value,                  // 3
                        key,        // 4
                        value));                // 5
    }

    // we don't actually delete shit from database, just setting value to 0
    public CompletableFuture<?> removeProperty(final int playerId, @NonNull String key) {
        return database.executeQuery(TextFormat.formatText("DELETE FROM properties WHERE GamerId = {} AND Key = '{}'", playerId, key));
    }


    //TODO: FIX THIS
    public CompletableFuture<TIntIntMap> loadTopPlayerProperties(
            final @NonNull String property,
            final boolean ascending,
            final int limit
    ) {
        return database.executeQuery(TextFormat.formatText(LOAD_TOP_PLAYER_PROPERTIES, property, property, ascending ? "ASC" : "DESC"))
                .thenApply(rows -> {
                    TIntIntMap topPlayers = new TIntIntHashMap(limit);

                    for (val row : rows) {
                        int playerId = row.getRequiredInt("Id");

                        topPlayers.put(playerId, row.getInt(property).orElse(0));

                        if (topPlayers.size() == limit) {
                            break;
                        }
                    }

                    return topPlayers;
                });
    }

/*    public void resetGlobalProperty(final @NonNull PropertyType propertyType,
                                    final int newValue) {
        database.executeQuery(TextFormat.formatText(WIPE_SPECIFIC_PROP,
                propertyType.name(), newValue));
    }*/
}
