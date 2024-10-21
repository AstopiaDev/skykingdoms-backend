package net.skykingdoms.api.backend.type.property;


import io.netty.buffer.ByteBuf;
import net.skykingdoms.api.backend.type.property.impl.BooleanPropertyType;
import net.skykingdoms.api.backend.type.property.impl.IntPropertyType;
import net.skykingdoms.api.backend.type.property.impl.LongPropertyType;
import net.skykingdoms.api.backend.type.property.impl.StringPropertyType;

public abstract class PropertyType<T> {
    protected T value;

    public abstract T deserialize(String data);
    public abstract void serialize(ByteBuf buf);
    public abstract String getInsertStatement(int playerId, String key);

    public final void setValue(T value) {
        this.value = value;
    }

    public final T getValue() {
        return value;
    }

    public static PropertyType<?> deserialize(String type, String data) {
        switch (type) {
            case "STRING":
                StringPropertyType stringPropertyType = new StringPropertyType();
                stringPropertyType.setValue(data);
                return stringPropertyType;

            case "INT":
                IntPropertyType integerPropertyType = new IntPropertyType();
                integerPropertyType.setValue(Integer.parseInt(data));
                return integerPropertyType;

            case "LONG":
                LongPropertyType longPropertyType = new LongPropertyType();
                longPropertyType.setValue(Long.parseLong(data));
                return longPropertyType;

            case "BOOLEAN":
                BooleanPropertyType booleanPropertyType = new BooleanPropertyType();
                booleanPropertyType.setValue(Boolean.parseBoolean(data));
                return booleanPropertyType;

            default:
                throw new IllegalArgumentException("Unknown property type: " + type);
        }
    }
}
