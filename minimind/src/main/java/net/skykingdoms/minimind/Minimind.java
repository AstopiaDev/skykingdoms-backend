package net.skykingdoms.minimind;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.backend.base.command.ICommand;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.util.collection.CollectionUtil;
import net.skykingdoms.backend.base.util.config.Configuration;
import net.skykingdoms.backend.base.util.config.ConfigurationProvider;
import net.skykingdoms.backend.base.util.config.YamlConfiguration;
import net.skykingdoms.backend.protocol.exception.ConnectException;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.server.ServerScaleType;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentServiceDataImpl;
import net.skykingdoms.minimind.command.CommandSender;
import net.skykingdoms.minimind.config.MinimindConfiguration;
import net.skykingdoms.minimind.connection.LocalClientConnection;
import net.skykingdoms.minimind.connection.MinimindConnection;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor
public abstract class Minimind implements IMinimind {
    @Getter ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    @Getter ExecutorService executor = Executors.newCachedThreadPool();

    Map<String, ICommand> commands = CollectionUtil.createConcurrentMap();

    String name;
    @NonFinal @Getter MinimindConfiguration configuration;
    @NonFinal @Getter FluentServiceDataImpl serviceData;
    @NonFinal InetSocketAddress address;
    @Getter @NonFinal LocalClientConnection connection;
    @NonFinal @Getter @Setter boolean reconnecting = false;

    public void bootstrap(String[] args) {
        preBootstrap();
        AsyncExecution.INSTANCE.init(this);

        this.reloadConfiguration();
        this.serviceData = new FluentServiceDataImpl();
        this.address = new InetSocketAddress(
                configuration.getRemoteCoordinatorHost(),
                configuration.getRemoteCoordinatorPort()
        );
        this.connection = new LocalClientConnection(this, address, 5345);

        try {
            connection.connectSynchronized();
            Logging.IMP.info("[Bootstrap] Synchronized connection invoked");
        } catch (ConnectException e) {
            e.printStackTrace();
            Logging.IMP.warn("[Bootstrap] Shutting down due to internal connection error");
            return;
        }

        MinimindConnection.setConnection(connection);
        postBootstrap();
    }

    public void registerCommand(final @NonNull String name,
                                final @NonNull ICommand command) {
        commands.put(name.toLowerCase(), command);
    }

    public void executeCommand(final @NonNull CommandSender sender,
                               final @NonNull String line) {
        AsyncExecution.INSTANCE.run(() -> _executeCommand(sender, line));
    }

    private void _executeCommand(final @NonNull CommandSender sender,
                                 @NonNull String line) {
        line = line.trim();

        if (!line.startsWith("/") || line.length() == 1) {
            return;
        }

        boolean isPlayer = sender instanceof AbstractGamer;

        String[] split = line.substring(1).split("\\s+");

        String label = split[0].toLowerCase();
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        if (isPlayer /*&& pluginManager.callEvent(new CommandEvent((Gamer) sender, label, args, false)).isCancelled()*/) {
            return;
        }

        val command = getCommand(label);

        if (command != null) {
            command.execute(sender, args);
        }
    }

    public void reloadConfiguration() {
        val provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        val configFile = new File("config.yml");

        try {
            if (!configFile.exists()) {
                Configuration defaultConfiguration = new Configuration();
                defaultConfiguration.set("coordinatorHost", "");
                defaultConfiguration.set("coordinatorPort", -1);
                defaultConfiguration.set("coordinatorSecret", "");

                //noinspection ResultOfMethodCallIgnored
                configFile.createNewFile();

                provider.save(defaultConfiguration, configFile);
            }

            Configuration yamlConfiguration = provider.load(configFile);

            configuration = new MinimindConfiguration(
                    yamlConfiguration.getString("coordinatorHost"),
                    yamlConfiguration.getInt("coordinatorPort"),
                    yamlConfiguration.getString("coordinatorSecret")
            );
        } catch (IOException e) {
            Logging.IMP.stacktrace(e, "Can't load configuration file");
        }
    }

    public ICommand getCommand(final @NonNull String name) {
        return commands.get(name.toLowerCase());
    }

    public Set<String> getCommands() {
        return commands.keySet();
    }
}
