package net.skykingdoms.backend.base.data.property.impl;

import net.skykingdoms.api.backend.type.property.PropertyType;

public class IntPropertyType extends PropertyType<Integer> {
    @Override
    public Integer deserialize(String data) {
        return this.value = Integer.parseInt(data);
    }

    @Override
    public String serialize() {
        return String.valueOf(this.value);
    }
}
