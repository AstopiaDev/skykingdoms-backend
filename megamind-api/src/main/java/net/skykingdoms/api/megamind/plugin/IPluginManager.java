package net.skykingdoms.api.megamind.plugin;

import lombok.NonNull;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.api.megamind.event.Listener;

import java.nio.file.Path;
import java.util.Collection;

public interface IPluginManager {
    void loadPlugins();
    void enablePlugins();
    void detectPlugins(final @NonNull Path folder);
    <T extends Event> T callEvent(final @NonNull T event);
    void registerListener(final @NonNull Listener listener);
    void unregisterListener(final @NonNull Listener listener);
    void unregisterListeners(final @NonNull MegamindPlugin plugin);
    Collection<? extends MegamindPlugin> getPlugins();
    <T extends MegamindPlugin> T getPlugin(final @NonNull String name);
}
