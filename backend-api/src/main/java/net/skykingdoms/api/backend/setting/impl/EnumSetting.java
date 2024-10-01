package net.skykingdoms.api.backend.setting.impl;

import lombok.Getter;
import net.skykingdoms.api.backend.setting.AbstractSetting;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Vihara
 */
@Getter
public final class EnumSetting<E extends Enum<E>> extends AbstractSetting<E, Integer> {

    Class<E> enumClass;

    // atomic for a reason........... (Jul 8 2023 incident)
    AtomicReference<E> value;

    public EnumSetting(E defaultValue) {
        this.enumClass = defaultValue.getDeclaringClass();
        this.value = new AtomicReference<>(defaultValue);
    }

    @Override
    public AbstractSetting<E, Integer> deserialize(final Integer val) {
        try {
            value.set(enumClass.getEnumConstants()[val]);
        } catch (Throwable throwable) { throwable.printStackTrace(); }
        return this;
    }

    @Override
    public Integer serialize() {
        return value.get().ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EnumSetting && value.get() == ((EnumSetting<?>) obj).getValue();
    }

    @Override
    public @NotNull E getValue() {
        return value.get();
    }

    @Override
    public void setValue(@NotNull E value) {
        this.value.set(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enumClass);
    }

    @Override
    public AbstractSetting<E, Integer> clone() {
        return new EnumSetting<>(value.get());
    }
}
