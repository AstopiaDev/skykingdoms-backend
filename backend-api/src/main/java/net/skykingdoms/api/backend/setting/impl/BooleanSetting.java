package net.skykingdoms.api.backend.setting.impl;

import net.skykingdoms.api.backend.setting.AbstractSetting;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 */
public final class BooleanSetting extends AbstractSetting<Boolean, Boolean> {

    public BooleanSetting(final boolean value) {
        this.value = value;
    }

    @Override
    public AbstractSetting<Boolean, Boolean> deserialize(Boolean val) {
        this.value = val;
        return this;
    }

    @Override
    public Boolean serialize() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BooleanSetting && value == ((BooleanSetting) obj).getValue();
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public void setValue(@NotNull Boolean value) {
        this.value = value;
    }

    @Override
    public BooleanSetting clone() {
        return new BooleanSetting(value);
    }
}
