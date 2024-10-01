package net.skykingdoms.backend.base.mysql;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

@FieldDefaults(level = AccessLevel.PRIVATE)
public final class DatabaseCredentials {
    public static final DatabaseCredentials MAIN
            = DatabaseCredentials.makePreset("mysql.main.properties", "mysql.main.properties.path");

    final Properties properties;
    final String database;

    public DatabaseCredentials(final @NonNull Properties properties,
                               final @Nullable String database) {
        this.properties = properties;
        this.database = database;

       // Logging.IMP.info("DatabaseCredentials: " + properties.toString() + " " + database);
    }

    public static @NotNull DatabaseCredentials makePreset(@NonNull String path,
                                                          final @NonNull String property) {
        path = System.getProperty(property, path);

        val actual = new Properties();
        val nioPath = Paths.get(path);

        if (Files.exists(nioPath)) {
            try (val is = Files.newInputStream(nioPath)) {
                actual.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new DatabaseCredentials(actual, null);
    }

    public @NotNull DatabaseCredentials withDatabase(final @NonNull String database) {
        return new DatabaseCredentials(properties, database);
    }

    @Nullable
    public String getDatabase() {
        return database;
    }

    @NotNull
    public String getUser() {
        return properties.getProperty("user");
    }

    @NotNull
    public String getPassword() {
        return properties.getProperty("password");
    }

    @NotNull
    public String getHost() {
        return properties.getProperty("host");
    }

    @NotNull
    public String[] getHosts() {
        return getHost().split(",\\s*");
    }

    public int getMaximumPoolSize() {
        return 32;
    }

    public int getPort() {
        return Integer.valueOf(properties.getProperty("port"));
    }

    public int @NotNull [] getPorts() {
        return Arrays.stream(properties.getProperty("port", "3306").split(",\\s*"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof DatabaseCredentials))
            return false;

        val preset = (DatabaseCredentials) o;

        return Objects.equals(database, preset.database)
                && Objects.equals(properties, preset.properties);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + (database == null ? 0 : database.hashCode());
        hash = 31 * hash + properties.hashCode();

        return hash;
    }

}
