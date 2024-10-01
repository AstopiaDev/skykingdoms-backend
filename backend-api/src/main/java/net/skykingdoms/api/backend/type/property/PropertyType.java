package net.skykingdoms.api.backend.type.property;

public abstract class PropertyType<T> {
    protected T value;

    public abstract T deserialize(String data);
    public abstract String serialize();

    public final void setValue(T value) {
        this.value = value;
    }

    public final T getValue() {
        return value;
    }
}
