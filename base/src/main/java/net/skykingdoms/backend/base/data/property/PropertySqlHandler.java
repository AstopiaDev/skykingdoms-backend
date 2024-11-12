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
import net.skykingdoms.api.backend.type.property.impl.BooleanPropertyType;
import net.skykingdoms.api.backend.type.property.impl.IntPropertyType;
import net.skykingdoms.api.backend.type.property.impl.LongPropertyType;
import net.skykingdoms.api.backend.type.property.impl.StringPropertyType;
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
        propertyTypeMap.put(Boolean.class, value -> new BooleanPropertyType());

        propertyMap.put("STRING", StringPropertyType::new);
        propertyMap.put("INT", IntPropertyType::new);
        propertyMap.put("LONG", LongPropertyType::new);
        propertyMap.put("BOOLEAN", BooleanPropertyType::new);
    }

    public CompletableFuture<Object2ObjectMap<String, PropertyType<?>>> loadProperties(final int playerId) {
        CompletableFuture<Object2ObjectMap<String, PropertyType<?>>> intPropertiesFuture = loadPropertiesTable("IntProperties", playerId, this::mapIntProperty);
        CompletableFuture<Object2ObjectMap<String, PropertyType<?>>> stringPropertiesFuture = loadPropertiesTable("StringProperties", playerId, this::mapStringProperty);
        CompletableFuture<Object2ObjectMap<String, PropertyType<?>>> longPropertiesFuture = loadPropertiesTable("LongProperties", playerId, this::mapLongProperty);
        CompletableFuture<Object2ObjectMap<String, PropertyType<?>>> booleanPropertiesFuture = loadPropertiesTable("BooleanProperties", playerId, this::mapBooleanProperty);


        return CompletableFuture.allOf(intPropertiesFuture, stringPropertiesFuture, longPropertiesFuture)
                .thenApply(v -> {
                    Object2ObjectMap<String, PropertyType<?>> allProperties = new Object2ObjectArrayMap<>();
                    allProperties.putAll(intPropertiesFuture.join());
                    allProperties.putAll(stringPropertiesFuture.join());
                    allProperties.putAll(longPropertiesFuture.join());
                    allProperties.putAll(booleanPropertiesFuture.join());
                    return allProperties;
                })
                .exceptionally(ex -> {
                    // Handle the exception, log it or return an empty map
                    ex.printStackTrace();
                    return new Object2ObjectArrayMap<>();
                });
    }

    private CompletableFuture<Object2ObjectMap<String, PropertyType<?>>> loadPropertiesTable(String tableName, int gamerId, Function<BufferedRow, PropertyType<?>> mapper) {
        return database.executeQuery("SELECT * FROM `" + tableName + "` WHERE `GamerId` = ?;", gamerId).thenApply(rows -> {
            Object2ObjectMap<String, PropertyType<?>> properties = new Object2ObjectArrayMap<>();
            rows.forEach((row) -> {
                PropertyType<?> propertyType = mapper.apply(row);
                properties.put(row.getRequiredString("Property"), propertyType);
            });
            return properties;
        });
    }

    private IntPropertyType mapIntProperty(BufferedRow row) {
        IntPropertyType propertyType = new IntPropertyType();
        propertyType.setValue(row.getRequiredInt("Value"));
        return propertyType;
    }

    private StringPropertyType mapStringProperty(BufferedRow row) {
        StringPropertyType propertyType = new StringPropertyType();
        propertyType.setValue(row.getRequiredString("Value"));
        return propertyType;
    }

    private LongPropertyType mapLongProperty(BufferedRow row) {
        LongPropertyType propertyType = new LongPropertyType();
        propertyType.setValue(row.getRequiredLong("Value"));
        return propertyType;
    }

    private BooleanPropertyType mapBooleanProperty(BufferedRow row) {
        BooleanPropertyType propertyType = new BooleanPropertyType();
        propertyType.setValue(row.getBoolean("Value"));
        return propertyType;
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
        return database.executeUpdate(property.getInsertStatement(playerId, key));
    }

    // we don't actually delete shit from database, just setting value to 0
    public CompletableFuture<?> removeProperty(final int playerId, @NonNull String key) {
        return database.executeVoidUpdate(TextFormat.formatText("DELETE FROM `StringProperties` WHERE GamerId = {} AND Property = '{}'", playerId, key));
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
