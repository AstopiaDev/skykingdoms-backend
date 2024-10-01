package net.skykingdoms.api.backend.setting;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * @author Vihara
 * @param <T> data type
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
@NoArgsConstructor
@ToString
public abstract class AbstractSetting<T, S> implements Setting<T, S> {

    T value;

    @Override
    public abstract AbstractSetting<T, S> clone();

}
