package net.skykingdoms.api.megamind;

import lombok.Getter;
import net.skykingdoms.api.megamind.plugin.IPluginManager;

import java.lang.reflect.Field;

@Getter
public abstract class MegamindAPI {
    protected IPluginManager pluginManager;

    public static MegamindAPI getInstance() {
        try {
            Class<?> megamindAppClass = Class.forName("net.skykingdoms.backend.megamind.MegamindApp");
            Field instanceField = megamindAppClass.getField("INSTANCE");

            Object instance = instanceField.get(null);
            if (instance instanceof MegamindAPI) {
                return (MegamindAPI) instance;
            } else {
                throw new IllegalStateException("INSTANCE is not of type MegamindAPI");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MegamindApp class not found", e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("INSTANCE field not found in MegamindApp", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Access to INSTANCE field is illegal", e);
        }
    }
}
