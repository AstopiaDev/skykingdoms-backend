package net.skykingdoms.backend.bukkit.startup;

import lombok.val;
import net.skykingdoms.backend.base.util.config.Configuration;
import net.skykingdoms.backend.base.util.config.ConfigurationProvider;
import net.skykingdoms.backend.base.util.config.YamlConfiguration;
import net.skykingdoms.backend.bukkit.startup.core.BukkitAgent;
import net.skykingdoms.backend.bukkit.startup.core.JavaVersion;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.server.GameMode;
import net.skykingdoms.backend.protocol.server.GameType;
import net.skykingdoms.backend.protocol.server.IConnectable;
import net.skykingdoms.backend.protocol.server.ServerScaleType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Agent that basically sets up everything on le instance 0_o
 *
 */
public final class BukkitAgentBootstrapper {

    // Entry-Point
    public static void main(final String @NotNull [] args) {
        Logging.IMP.info("[entrypoint] Bootstrapping agent driver ...");

        Configuration configuration = loadConfiguration();

        JavaVersion javaVersion = null;

        if (System.getProperty("java") != null) {
            Logging.IMP.info("[entrypoint] Running in Java " + System.getProperty("java"));
            javaVersion = JavaVersion.valueOf(System.getProperty("java"));
        }

        try {
            new BukkitAgent().bootstrap(
                    ServerScaleType.valueOf(configuration == null ? System.getenv("SERVER_SCALE") : configuration.getString("SERVER_SCALE")),
                    GameType.valueOf(configuration == null ? System.getenv("GAME_TYPE") : configuration.getString("GAME_TYPE")),
                    GameMode.valueOf(configuration == null ? System.getenv("GAME_MODE") : configuration.getString("GAME_MODE"), null), configuration, javaVersion
            );
        } catch (Throwable throwable) {
            Logging.IMP.stacktrace(throwable, "Internal error occurred");
        }
    }

    public static Configuration loadConfiguration() {
        val provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        val configFile = new File("config.yml");
        Configuration configuration = null;

        if (configFile.exists()) {
            try {
                configuration = provider.load(configFile);
            } catch (IOException e) {
                Logging.IMP.stacktrace(e, "Can't load configuration file");
            }
        }

        return configuration;
    }

}
