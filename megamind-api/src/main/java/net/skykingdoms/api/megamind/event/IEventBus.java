package net.skykingdoms.api.megamind.event;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * TODO: Rewrite event bus in a better and faster way
 */
public interface IEventBus {

    Map<Class<?>, Map<Byte, Map<Object, Method[]>>> byListenerAndPriority = new HashMap<>();
    Map<Class<?>, EventHandlerMethod[]> byEventBaked = new HashMap<>();

    void onPost(final @NonNull Event event);

    default void post(final @NonNull Event event) {
        EventHandlerMethod[] handlers = byEventBaked.get(event.getClass());

        if (handlers != null) {
            for (val method : handlers) {
                try {
                    method.invoke(event);
                } catch (IllegalAccessException ex) {
                    throw new Error("Method became inaccessible: " + event, ex);
                } catch (IllegalArgumentException ex) {
                    throw new Error("Method rejected target/argument: " + event, ex);
                } catch (InvocationTargetException ex) {
                    Logging.IMP.stacktrace(ex.getCause(), "Error dispatching event {} to handler {}",
                            event, method.getListener());
                }
            }
        }

        onPost(event);
    }

    private Map<Class<?>, Map<Byte, Set<Method>>> findHandlers(final @NonNull Object listener) {
        Map<Class<?>, Map<Byte, Set<Method>>> handler = new HashMap<>();

        for (val method : listener.getClass().getDeclaredMethods()) {
            val annotation = method.getAnnotation(EventHandler.class);

            if (annotation != null) {
                Class<?>[] params = method.getParameterTypes();

                if (params.length != 1) {
                    Logging.IMP.error("Method {} in class {} annotated with {} does not have single argument",
                            method, listener.getClass(), annotation);

                    continue;
                }

                handler
                        .computeIfAbsent(params[0], k -> new HashMap<>())
                        .computeIfAbsent(annotation.priority(), k -> new HashSet<>())
                        .add(method);
            }
        }

        return handler;
    }

    default void register(final @NonNull Object listener) {
        val handler = findHandlers(listener);

        for (val e : handler.entrySet()) {
            val prioritiesMap = byListenerAndPriority.computeIfAbsent(e.getKey(), k -> new HashMap<>());

            for (Map.Entry<Byte, Set<Method>> entry : e.getValue().entrySet()) {
                val currentPriorityMap = prioritiesMap.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
                val baked = new Method[entry.getValue().size()];
                currentPriorityMap.put(listener, entry.getValue().toArray(baked));
            }

            bakeHandlers(e.getKey());
        }
    }

    default void unregister(final @NonNull Object listener) {
        val handler = findHandlers(listener);

        for (val e : handler.entrySet()) {
            val prioritiesMap = byListenerAndPriority.get(e.getKey());

            if (prioritiesMap != null) {
                for (val priority : e.getValue().keySet()) {
                    val currentPriority = prioritiesMap.get(priority);

                    if (currentPriority != null) {
                        currentPriority.remove(listener);

                        if (currentPriority.isEmpty()) {
                            prioritiesMap.remove(priority);
                        }
                    }
                }

                if (prioritiesMap.isEmpty()) {
                    byListenerAndPriority.remove(e.getKey());
                }
            }
            bakeHandlers(e.getKey());
        }
    }

    private void bakeHandlers(Class<?> eventClass) {
        val handlersByPriority = byListenerAndPriority.get(eventClass);

        if (handlersByPriority != null) {
            List<EventHandlerMethod> handlersList = new ArrayList<>(handlersByPriority.size() * 2);

            // Iterate through all priorities from Byte.MIN_VALUE to Byte.MAX_VALUE
            byte value = Byte.MIN_VALUE;

            do {
                val handlersByListener = handlersByPriority.get(value);

                if (handlersByListener != null) {
                    for (val listenerHandlers : handlersByListener.entrySet()) {
                        for (Method method : listenerHandlers.getValue()) {
                            EventHandlerMethod ehm = new EventHandlerMethod(listenerHandlers.getKey(), method);
                            handlersList.add(ehm);
                        }
                    }
                }
            } while (value++ < Byte.MAX_VALUE);

            byEventBaked.put(eventClass, handlersList.toArray(new EventHandlerMethod[0]));
        } else {
            byEventBaked.put(eventClass, null);
        }
    }
}
