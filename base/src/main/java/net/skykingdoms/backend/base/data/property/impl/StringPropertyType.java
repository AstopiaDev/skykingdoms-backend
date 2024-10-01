package net.skykingdoms.backend.base.data.property.impl;

import net.skykingdoms.api.backend.type.property.PropertyType;

public class StringPropertyType extends PropertyType<String> {
    @Override
    public String deserialize(String data) {
        this.value = data;
        return data;
    }

    @Override
    public String serialize() {
        return value;
    }
}
