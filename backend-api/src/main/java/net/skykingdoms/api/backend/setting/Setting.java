package net.skykingdoms.api.backend.setting;

import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 * @param <T>
 */
public interface Setting<T, S> extends Cloneable {

    /**
     * Returns setting value
     */
    @NotNull T getValue();

    /**
     * Sets a new setting value
     */
    void setValue(final @NotNull T value);

    /**
     * Deserializes AbstractSetting from mask
     *
     * @param val   database value
     * @return      setting instance
     */
    Setting<T, S> deserialize(final S val);

    /**
     * Deserializes AbstractSetting from mask
     *
     * @return      serialized integer value for database
     */
    S serialize();

    Setting<T, S> clone();

    /**
     * cocked
     */
    @NotNull String toString();

}
