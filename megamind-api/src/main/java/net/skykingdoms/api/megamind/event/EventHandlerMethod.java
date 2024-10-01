package net.skykingdoms.api.megamind.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter
public final class EventHandlerMethod {

    @NonNull Object listener;
    @NonNull Method method;

    public void invoke(Object event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        method.invoke(listener, event);
    }

}
