package net.skykingdoms.api.megamind.plugin;

import lombok.Getter;
import lombok.NonNull;
import net.skykingdoms.api.backend.util.config.Configuration;
import net.skykingdoms.api.backend.util.config.ConfigurationProvider;
import net.skykingdoms.api.backend.util.config.YamlConfiguration;
import net.skykingdoms.api.protocol.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public abstract class MegamindPlugin {
    @Getter
    private PluginDescription description;

    @Getter
    private Path path;

    private File configFile;

    @Getter
    private Configuration config;

    private YamlConfiguration yaml;

    /**
     * Called when the plugin has just been loaded. Most of the proxy will not
     * be initialized, so only use it for registering
     */
    public void onLoad() {

    }

    /**
     * Called when this plugin is enabled.
     */
    public void onEnable() {

    }

    /**
     * Called when this plugin is disabled.
     */
    public void onDisable() {

    }

    public final File getDataFolder() {
        return getDataPath().toFile();
    }

    public abstract Path getDataPath();

    /**
     * Get a resource from within this plugins jar or container. Care must be
     * taken to close the returned stream.
     *
     * @param name the full path name of this resource
     * @return the stream for getting this resource, or null if it does not
     * exist
     */
    public final InputStream getResourceAsStream(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    /**
     * Called by the loader to initialize the fields in this plugin.
     *
     * @param description the description that describes this plugin
     */
    public final void init(@NonNull PluginDescription description) {
        this.description = description;
        this.path = description.getPath();

        loadConfig();
    }

    public void loadConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        yaml = (YamlConfiguration) ConfigurationProvider.getProvider(YamlConfiguration.class);
        if (configFile.exists()) {
            try {
                config = yaml.load(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveConfig() {
        try {
            yaml.save(config, configFile);
        } catch (IOException ex) {
            Logging.IMP.stacktrace(ex, "[{}] Error occurred whilst saving a configuration",
                    getDescription().getName());
        }
    }

    public void saveDefaultConfig() {
        if (config != null) {
            return;
        }

        try {
            if (!getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                getDataFolder().mkdir();
            }

            // fixme: returns coordinator's configuration file
            // advised solution: rename coordinator config to `coordinator.yml`
            // and keep `config.yml` name for plugin :3
            if (!configFile.exists()) {
                InputStream is = getResourceAsStream("config.yml");
                if (is == null) {
                    throw new IllegalStateException("config.yml not found in plugin jar file");
                }

                yaml.save(yaml.load(is), configFile);
                loadConfig();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
