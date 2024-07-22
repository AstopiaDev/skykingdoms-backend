package net.skykingdoms.backend.megamind.api.plugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.event.EventBus;
import net.skykingdoms.backend.protocol.logging.Logging;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PluginManager {
    Yaml yaml;
    EventBus eventBus;
    Map<String, Plugin> plugins = new LinkedHashMap<>();

    Multimap<Plugin, Listener> listenersByPlugin = ArrayListMultimap.create();
    @NonFinal Map<String, PluginDescription> toLoad = new HashMap<>();

    public PluginManager() {
        // Ignore unknown entries in the plugin descriptions
        val yamlConstructor = new Constructor(new LoaderOptions());

        val propertyUtils = yamlConstructor.getPropertyUtils();
        propertyUtils.setSkipMissingProperties(true);

        yamlConstructor.setPropertyUtils(propertyUtils);

        yaml = new Yaml(yamlConstructor);
        eventBus = new EventBus();
    }

    public Collection<Plugin> getPlugins() {
        return plugins.values();
    }

    public Plugin getPlugin(final @NonNull String name) {
        return plugins.get(name);
    }

    public void loadPlugins() {
        Map<PluginDescription, Boolean> pluginStatuses = new HashMap<>();
        for (val entry : toLoad.entrySet()) {
            PluginDescription plugin = entry.getValue();
            if (!enablePlugin(pluginStatuses, new Stack<>(), plugin)) {
                Logging.IMP.error("Failed to enable {}", entry.getKey());
            }
        }
        toLoad.clear();
        toLoad = null;
    }

    public void enablePlugins() {
        for (Plugin plugin : plugins.values()) {
            PluginDescription description = plugin.getDescription();

            try {
                plugin.onEnable();

                Logging.IMP.info(
                        "Enabled plugin {} version {} by {}",
                        description.getName(),
                        description.getVersion(),
                        description.getAuthor()
                );
            } catch (Throwable t) {
                Logging.IMP.stacktrace(t, "Exception encountered when loading plugin: {}",
                        description.getName());
            }
        }
    }

    private boolean enablePlugin(final @NonNull Map<PluginDescription, Boolean> pluginStatuses,
                                 final @NonNull Stack<PluginDescription> dependStack,
                                 final @NonNull PluginDescription plugin) {
        if (pluginStatuses.containsKey(plugin)) {
            return pluginStatuses.get(plugin);
        }

        // combine all dependencies for 'for loop'
        Set<String> dependencies = new HashSet<>();
        dependencies.addAll(plugin.getDepends());
        dependencies.addAll(plugin.getSoftDepends());

        // success status
        var status = true;

        // try to load dependencies first
        for (String dependName : dependencies) {
            PluginDescription depend = toLoad.get(dependName);
            Boolean dependStatus = (depend != null) ? pluginStatuses.get(depend) : Boolean.FALSE;

            if (dependStatus == null) {
                if (dependStack.contains(depend)) {
                    StringBuilder dependencyGraph = new StringBuilder();
                    for (PluginDescription element : dependStack) {
                        dependencyGraph.append(element.getName()).append(" -> ");
                    }
                    dependencyGraph.append(plugin.getName()).append(" -> ").append(dependName);

                    Logging.IMP.warn("Circular dependency detected: {}", dependencyGraph);

                    status = false;
                } else {
                    dependStack.push(plugin);
                    dependStatus = this.enablePlugin(pluginStatuses, dependStack, depend);
                    dependStack.pop();
                }
            }

            if (dependStatus == Boolean.FALSE && plugin.getDepends().contains(dependName)) // only fail if this wasn't a soft dependency
            {
                Logging.IMP.error("{} (required by {}) is unavailable", String.valueOf(dependName), plugin.getName());

                status = false;
            }

            if (!status) {
                break;
            }
        }

        // do actual loading
        if (status) {
            try {
                URLClassLoader loader = new PluginClassloader(new URL[]{plugin.getPath().toUri().toURL()});
                Class<?> main = loader.loadClass(plugin.getMain());
                Plugin clazz = (Plugin) main.getDeclaredConstructor().newInstance();

                clazz.init(plugin);
                plugins.put(plugin.getName(), clazz);
                clazz.onLoad();

                Logging.IMP.info("Loaded plugin {} version {} by {}", plugin.getName(), plugin.getVersion(), plugin.getAuthor());
            } catch (Throwable t) {
                Logging.IMP.warn("Error enabling plugin {}", plugin.getName(), t);
                t.printStackTrace();
            }
        }

        pluginStatuses.put(plugin, status);
        return status;
    }

    public void detectPlugins(final @NonNull Path folder) {
        if (!Files.isDirectory(folder)) {
            throw new IllegalStateException(folder + " must be a directory");
        }

        try {
            Files.list(folder)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .forEach(path -> {
                        try (JarFile jar = new JarFile(path.toFile())) {
                            JarEntry pluginData = jar.getJarEntry("coordinator.yml");

                            if (pluginData == null) {
                                pluginData = jar.getJarEntry("plugin.yml");
                            }

                            Preconditions.checkNotNull(pluginData, "Plugin must have a plugin.yml or coordinator.yml");

                            try (InputStream in = jar.getInputStream(pluginData)) {
                                PluginDescription desc = yaml.loadAs(in, PluginDescription.class);
                                desc.setPath(path);

                                toLoad.put(desc.getName(), desc);
                            }
                        } catch (Exception ex) {
                            Logging.IMP.stacktrace(ex, "Unable to load plugin from file {}", path);
                        }
                    });
        } catch (IOException e) {
            Logging.IMP.stacktrace(e, "Can't list files");
        }
    }

    public <T extends Event> T callEvent(final @NonNull T event) {
        Preconditions.checkNotNull(event, "event");

        long start = System.nanoTime();
        eventBus.post(event);
        event.postCall();

        long elapsed = start - System.nanoTime();

        if (elapsed > 250000) {
            Logging.IMP.warn("Event {} took more {} ns to process!", event, elapsed);
        }

        return event;
    }

    public void registerListener(final @NonNull Listener listener) {
        eventBus.register(listener);
    }

    public void unregisterListener(final @NonNull Listener listener) {
        eventBus.unregister(listener);
        listenersByPlugin.values().remove(listener);
    }

    public void unregisterListeners(final @NonNull Plugin plugin) {
        for (Iterator<Listener> it = listenersByPlugin.get(plugin).iterator(); it.hasNext(); ) {
            eventBus.unregister(it.next());
            it.remove();
        }
    }

}
