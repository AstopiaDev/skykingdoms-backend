package net.skykingdoms.backend.base.data.property.impl;

import net.skykingdoms.api.backend.type.property.PropertyType;

public class LongPropertyType extends PropertyType<Long> {
    @Override
    public Long deserialize(String data) {
        return this.value = Long.parseLong(data);
    }

    @Override
    public String serialize() {
        return String.valueOf(value);
    }
}
