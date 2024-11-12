package net.skykingdoms.backend.base.dependency;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class DependencyInjection {

    private static final Map<Class<?>, Object> LOADED_DEPENDENCIES = new HashMap<>();

    public static void putDependency(Object dependency) {
        LOADED_DEPENDENCIES.put(dependency.getClass(), dependency);
    }

    public static void injectDependencies(Class<?> cls, Object object) {
        for (Field field : cls.getDeclaredFields()) {

            if (!field.isAnnotationPresent(Dependency.class)) {
                continue;
            }

            Object dependency = loadDependency(field.getType());

            try {
                field.setAccessible(true);

                try {
                    field.set(object, dependency);
                } finally {
                    field.setAccessible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Object loadDependency(Class<?> cls) {
        // смотрим, загружена ли зависимость
        Object alreadyLoadedDependency = LOADED_DEPENDENCIES.get(cls);

        if (alreadyLoadedDependency != null) {
            return alreadyLoadedDependency;
        }

        // зависимость не загружена, ищем в этой зависимости поле с аннотацией Dependency

        // потом перебираем все поля
        for (Field field : cls.getDeclaredFields()) {
            // потом смотрим, если у этого поля аннотация Dependency,
            // если нет, то пропускаем его
            if (!field.isAnnotationPresent(Dependency.class)) {
                continue;
            }

            // потом идёт проверка на статик
            if (!Modifier.isStatic(field.getModifiers())) {
                throw new IllegalStateException("Can't load dependency: instance field must be static");
            }

            try {
                field.setAccessible(true);

                // дальше получаем Instance этой зависимости
                try {
                    Object instance = field.get(null);

                    LOADED_DEPENDENCIES.put(cls, instance);

                    return instance;
                } finally {
                    field.setAccessible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        throw new IllegalStateException("Can't load dependency: instance field not found");
    }

}
