package net.skykingdoms.api.backend.util.config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigurationProvider {

    public static final ConfigurationProvider YAML = new YamlConfiguration();

    private static final Map<Class<? extends ConfigurationProvider>, ConfigurationProvider> PROVIDER_MAP = new HashMap<>();

    static {
        PROVIDER_MAP.put(YAML.getClass(), YAML);
    }

    public static ConfigurationProvider getProvider(Class<? extends ConfigurationProvider> provider) {
        return PROVIDER_MAP.get(provider);
    }
    /*------------------------------------------------------------------------*/

    public abstract void save(Configuration config, File file) throws IOException;

    public abstract void save(Configuration config, Writer writer);

    public abstract Configuration load(File file) throws IOException;

    public abstract Configuration load(File file, Configuration defaults) throws IOException;

    public abstract Configuration load(Reader reader);

    public abstract Configuration load(Reader reader, Configuration defaults);

    public abstract Configuration load(InputStream is);

    public abstract Configuration load(InputStream is, Configuration defaults);

    public abstract Configuration load(String string);

    public abstract Configuration load(String string, Configuration defaults);

}
