package net.skykingdoms.api.backend.util.config;

import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlConfiguration extends ConfigurationProvider {

    private final ThreadLocal<Yaml> yaml = ThreadLocal.withInitial(() -> {
        val options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    });

    @Override
    public void save(Configuration config, File file) throws IOException {
        try (val writer = new FileWriter(file)) {
            save(config, writer);
        }
    }

    @Override
    public void save(final @NonNull Configuration config,
                     final @NonNull Writer writer) {
        yaml.get().dump(config.self, writer);
    }

    @Override
    public Configuration load(final @NonNull File file) throws IOException {
        return load(file, null);
    }

    @Override
    public Configuration load(final @NonNull File file,
                              final @Nullable Configuration defaults) throws IOException {
        try (val reader = new FileReader(file)) {
            return load(reader, defaults);
        }
    }

    @Override
    public Configuration load(final @NonNull Reader reader) {
        return load(reader, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(final @NonNull Reader reader,
                              final @Nullable Configuration defaults) {
        Map<String, Object> map = yaml.get().loadAs(reader, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        return new Configuration(map, defaults);
    }

    @Override
    public Configuration load(final @NonNull InputStream is) {
        return load(is, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(final @NonNull InputStream is,
                              final @Nullable Configuration defaults) {
        var map = yaml.get().loadAs(is, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        return new Configuration(map, defaults);
    }

    @Override
    public Configuration load(final @NonNull String string) {
        return load(string, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(final @NonNull String string,
                              final @Nullable Configuration defaults) {
        var map = yaml.get().loadAs(string, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        return new Configuration(map, defaults);
    }

}
