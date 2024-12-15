package net.skykingdoms.backend.megamind;

import com.google.common.collect.Streams;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.api.backend.command.ICommand;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.backend.util.config.Configuration;
import net.skykingdoms.api.backend.util.config.ConfigurationProvider;
import net.skykingdoms.api.backend.util.config.YamlConfiguration;
import net.skykingdoms.api.megamind.MegamindAPI;
import net.skykingdoms.api.megamind.event.impl.gamer.CommandEvent;
import net.skykingdoms.api.megamind.plugin.PluginDescription;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.api.protocol.type.NetworkType;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.base.command.tabcomplete.AbstractTabcompleter;
import net.skykingdoms.backend.base.command.tabcomplete.TabcompleteManager;
import net.skykingdoms.backend.base.data.stat.StatsManager;
import net.skykingdoms.backend.base.data.state.NetworkState;
import net.skykingdoms.backend.base.dependency.Dependency;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CommandSender;
import net.skykingdoms.backend.megamind.api.command.CoreCommandManager;
import net.skykingdoms.backend.megamind.command.*;
import net.skykingdoms.backend.megamind.command.auth.*;
import net.skykingdoms.backend.megamind.command.condition.CoreCommandConditions;
import net.skykingdoms.backend.megamind.connection.LocalServerConnection;
import net.skykingdoms.backend.megamind.data.auth.AuthManager;
import net.skykingdoms.backend.megamind.data.friend.FriendListener;
import net.skykingdoms.backend.megamind.data.friend.FriendManager;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.data.group.GroupListener;
import net.skykingdoms.backend.megamind.data.punishment.PunishmentListener;
import net.skykingdoms.backend.megamind.data.punishment.PunishmentManager;
import net.skykingdoms.backend.megamind.data.punishment.command.impl.*;
import net.skykingdoms.backend.megamind.data.staffchat.StaffChatListener;
import net.skykingdoms.backend.megamind.lookup.LookupManager;
import net.skykingdoms.backend.megamind.manager.*;
import net.skykingdoms.backend.megamind.plugin.PluginManager;
import net.skykingdoms.backend.megamind.server.impl.NodeServer;
import net.skykingdoms.backend.megamind.server.impl.ProxyServer;
import net.skykingdoms.backend.protocol.exception.BindException;
import net.skykingdoms.backend.protocol.server.IConnectable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Getter
public class MegamindApp extends MegamindAPI {

    // ----------------------------------------------------------------------
    @Dependency
    public static final MegamindApp INSTANCE = new MegamindApp();
    // ----------------------------------------------------------------------

    NetworkType networkType = NetworkType.valueOf(System.getProperty("network.type", NetworkType.DEVELOPMENT.name()));
    Map<String, ICommand> commands = CollectionUtil.createConcurrentMap();
    CoreCommandManager commandManager = new CoreCommandManager();

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    ExecutorService executor = Executors.newCachedThreadPool();

    Path directory = Paths.get(".").toAbsolutePath().normalize();
    Path plugins = directory.resolve("plugins");

    long startDate = System.nanoTime();

    @Setter @NonFinal NetworkState networkState;
    @NonFinal LocalServerConnection connection;
    @NonFinal Configuration configuration;
    @NonFinal boolean running = false;
    @NonFinal String secret;

    public void init() {
        Logging.IMP.info("[Megamind] Booting application ...");
        Language.reloadLanguages();
        Logging.IMP.info("[Megamind] Loaded languages");
        this.reloadConfiguration();
        Logging.IMP.info("[Megamind] Loaded configuration");
        networkState = NetworkState.valueOf(configuration.getString("state"));
        Logging.IMP.info("[Megamind] Loaded network state: {}", networkState);

        commandManager.addCondition(CoreCommandConditions.class);
        registerCommandCompleters();
        registerCommands();

        pluginManager = new PluginManager();
        this.enablePlugins();
        Logging.IMP.info("[Megamind] Loaded plugins");

        this.registerListeners();
        Logging.IMP.info("Listeners registered");

        ServerManager.INSTANCE.postBootstrap();
        CoreSecurityManager.INSTANCE.postBootstrap();
        DeploymentManager.INSTANCE.postBootstrap();
        ProxyManager.INSTANCE.postBootstrap();
        LobbyManager.INSTANCE.postBootstrap();
        FriendManager.INSTANCE.postBootstrap();
        PunishmentManager.INSTANCE.postBootstrap();
        TestFlowManager.INSTANCE.postBootstrap();
        StatsManager.INSTANCE.postBootstrap();
        DuelManager.INSTANCE.postBootstrap();
        AuthManager.INSTANCE.postBootstrap();
        Logging.IMP.info("[Megamind] Finished loading managers!");

        connection = new LocalServerConnection(
                new InetSocketAddress(
                        configuration.getString("host"),
                        configuration.getInt("port")), 0);
        try {
            connection.bindSynchronized();
        } catch (final BindException e) {
            Logging.IMP.stacktrace(e, "Can't bind to the specified address");
            return;
        }

        running = true;

        Logging.IMP.info("[Megamind] Connection opened at {}", connection.getChannel().localAddress());
        Logging.IMP.info("[Megamind] Epoll: {}", coloredEpoll(connection.isEpoll()));
        Logging.IMP.info("[Megamind] Megamind launched in {}ms",
                coloredStartupTime(getUptime(TimeUnit.MILLISECONDS)));

        new MegamindConsole().start();
    }

    public void shutdown() {
        running = false;
        Logging.IMP.info("Shutting down Megamind application ...");
        connection.closeConnection();

        // Disable lookups to avoid sending to servers online etc.
        Logging.IMP.info("Clearing lookups ...");
        LookupManager.INSTANCE.clearLookups();

        // kicking faggots
        Logging.IMP.info("Kicking players ...");
        Gamer.kickAll();

        Logging.IMP.info("Disabling plugins ...");
        for (val plugin : pluginManager.getPlugins()) {
            try {
                plugin.onDisable();
            } catch (Exception ex) {
                Logging.IMP.error("Unable to disable plugin {}", plugin.getDescription().getName(), ex);
            }
        }
        Logging.IMP.info("Megamind application has been shut down");
        System.exit(0);
    }

    private @NotNull String coloredStartupTime(final long time) {
        return (time > 1000
                ? time > 2500
                ? time > 5000
                ? ChatColor.RED
                : ChatColor.GOLD
                : ChatColor.YELLOW
                : ChatColor.GREEN)
                + String.valueOf(time);
    }

    public long getUptime(final @NonNull TimeUnit unit) {
        return unit.convert(System.nanoTime() - startDate, TimeUnit.NANOSECONDS);
    }

    private void reloadConfiguration() {
        val provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        val configFile = new File("config.yml");

        try {
            if (!configFile.exists()) {
                Configuration defaultConfiguration = new Configuration();
                defaultConfiguration.set("host", "0.0.0.0");
                defaultConfiguration.set("port", 5345);
                defaultConfiguration.set("secret", "");
                defaultConfiguration.set("state", NetworkState.OPEN.name());
                //defaultConfiguration.set("servers", new ArrayList<>());

                //noinspection ResultOfMethodCallIgnored
                configFile.createNewFile();

                provider.save(defaultConfiguration, configFile);
            }

            configuration = provider.load(configFile);
            secret = configuration.getString("secret");
        } catch (IOException e) {
            Logging.IMP.stacktrace(e, "Can't load configuration file");
        }
    }

    public void enablePlugins() {
        if (!Files.exists(plugins)) {
            try {
                Files.createDirectories(plugins);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create a 'plugins' directory", e);
            }
        }

        pluginManager.detectPlugins(plugins);

        pluginManager.loadPlugins();

        pluginManager.enablePlugins();
    }

    public List<String> tabComplete(@NonNull CommandSender sender, @NonNull String line) {
        if (line.startsWith("/")) {
            line = line.substring(1);
        }

        String[] params = line.split(" +", -1);
        if (params.length == 0) {
            return Collections.emptyList();
        }

        ICommand command = getCommand(params[0]);
        if (command == null) {
            return Collections.emptyList();
        }

        try {
            List<String> suggests = command.tabcomplete(sender, Arrays.copyOfRange(params, 1, params.length));
            return suggests != null ? suggests : Collections.emptyList();
        } catch (IndexOutOfBoundsException e) {
            return Collections.emptyList();
        }
    }

    public ICommand getCommand(final @NonNull String name) {
        return commands.get(name.toLowerCase());
    }

    public Set<String> getCommands() {
        return commands.keySet();
    }

    public void registerCommand(final @NonNull String name,
                                final @NonNull ICommand command) {
        commands.put(name.toLowerCase(), command);
    }

    public void removeCommand(final @NonNull String name) {
        commands.remove(name.toLowerCase());
    }

    private void registerCommandCompleters() {
        TabcompleteManager.IMP.addCompleter("void", new AbstractTabcompleter() {
            @Override
            public List<String> complete(final @NonNull ICommandSender sender,
                                         final @NonNull String label) {
                return Collections.emptyList();
            }
        });

        TabcompleteManager.IMP.addCompleter("playerName", new AbstractTabcompleter() {
            @Override
            public List<String> complete(final @NonNull ICommandSender sender,
                                         final @NonNull String label) {
                return any(label, Gamer.getGamers().stream().map(CommonGamer::getName));
            }
        });

        TabcompleteManager.IMP.addCompleter("remote", new AbstractTabcompleter() {
            @Override
            public @Nullable List<String> complete(@NotNull ICommandSender sender, @NotNull String label) {
                if (sender instanceof Gamer && !((Gamer) sender).isStaff()) {
                    return Collections.emptyList();
                }
                return any(label, DeploymentManager.INSTANCE.getAllDeployedInstances().stream().distinct()
                        .map(IConnectable::getName));
            }
        });

        // basically a server that player can join
        // it wont show a raw bukkit instances if games present
        TabcompleteManager.IMP.addCompleter("connectable", new AbstractTabcompleter() {
            @Override
            public @Nullable List<String> complete(@NotNull ICommandSender sender, @NotNull String label) {
                if (sender instanceof Gamer && !((Gamer) sender).isStaff()) {
                    return Collections.emptyList();
                }
                return any(label, Streams.concat(ServerManager.INSTANCE.getGameServers().stream(), LobbyManager.INSTANCE.getRegisteredLobbyServers().stream(), ServerManager.INSTANCE.getPersistentServers().stream())
                        .distinct()
                        .map(IConnectable::getName));
            }
        });

        TabcompleteManager.IMP.addCompleter("remoteType", new AbstractTabcompleter() {
            @Override
            public @Nullable List<String> complete(@NotNull ICommandSender sender, @NotNull String label) {
                return any(label, Arrays.stream(RemoteConnectedType.VALUES)
                        .map(RemoteConnectedType::name));
            }
        });

        TabcompleteManager.IMP.addCompleter("gameMode", new AbstractTabcompleter() {
            @Override
            public @Nullable List<String> complete(@NotNull ICommandSender sender, @NotNull String label) {
                return any(label, Arrays.stream(GameMode.VALUES)
                        .map(GameMode::name));
            }
        });

        TabcompleteManager.IMP.addCompleter("setting", new AbstractTabcompleter() {
            @Override
            public @Nullable List<String> complete(@NotNull ICommandSender sender, @NotNull String label) {
                if (sender instanceof Gamer && !((Gamer) sender).isStaff()) {
                    return Collections.emptyList();
                }
                return any(label, Arrays.stream(SettingType.VALUES)
                        .map(SettingType::name));
            }
        });

        TabcompleteManager.IMP.addCompleter("node", new AbstractTabcompleter() {
            @Override
            public List<String> complete(final @NonNull ICommandSender sender,
                                         final @NonNull String label) {
                if (sender instanceof Gamer && !((Gamer) sender).isStaff()) {
                    return Collections.emptyList();
                }
                if (sender instanceof Gamer && !((Gamer) sender).isStaff()) {
                    return Collections.emptyList();
                }
                return any(label, NodeManager.INSTANCE.getRegisteredNodes().stream()
                        .map(NodeServer::getName));
            }
        });

        TabcompleteManager.IMP.addCompleter("proxy", new AbstractTabcompleter() {
            @Override
            public List<String> complete(final @NonNull ICommandSender sender,
                                         final @NonNull String label) {
                if (sender instanceof Gamer && !((Gamer) sender).isStaff()) {
                    return Collections.emptyList();
                }
                return any(label, ProxyManager.INSTANCE.getProxyServers().values().stream()
                        .map(ProxyServer::getName));
            }
        });

        TabcompleteManager.IMP.addCompleter("rank", new AbstractTabcompleter() {
            @Override
            public List<String> complete(@NotNull ICommandSender sender, @NotNull String label) {
                return any(label, Arrays.stream(Group.VALUES).map(Group::name));
            }
        });

        TabcompleteManager.IMP.addCompleter("pluginName", new AbstractTabcompleter() {
            @Override
            public List<String> complete(final @NonNull ICommandSender sender,
                                         final @NonNull String label) {
                return any(label, pluginManager.getPlugins().stream().map((plugin) -> {
                    PluginDescription description = plugin.getDescription();
                    return description.getName();
                }));
            }
        });
    }

    private void registerListeners() {
        new FriendListener();
        new GroupListener();
        new PunishmentListener();
        new StaffChatListener();
    }

    public void registerCommands() {
        commandManager.addCommand(VanishCommand.class);
        commandManager.addCommand(MindReloadCommand.class);
        commandManager.addCommand(ShutdownCommand.class);
        commandManager.addCommand(DeployLobbyCommand.class);
        commandManager.addCommand(DeployGameInstancesCommand.class);
        commandManager.addCommand(PortCommand.class);
        commandManager.addCommand(KickCommand.class);
        commandManager.addCommand(PlayCommand.class);
        commandManager.addCommand(NetworkStateCommand.class);
        commandManager.addCommand(AlertCommand.class);
        commandManager.addCommand(DebugCommand.class);
        commandManager.addCommand(RequestNodeSnapshotCommand.class);
        commandManager.addCommand(ProxyCommand.class);
        commandManager.addCommand(DuelInternalCommand.class);
        commandManager.addCommand(NicknameCommand.class);
        commandManager.addCommand(FindCommand.class);
        commandManager.addCommand(GlistCommand.class);
        commandManager.addCommand(NodeListCommand.class);
        commandManager.addCommand(DeployTestCommand.class);
        commandManager.addCommand(InternalRankCommand.class);
        commandManager.addCommand(DestroyInstanceCommand.class);
        commandManager.addCommand(LocationRawCommand.class);
        commandManager.addCommand(ReloadLanguagesCommand.class);
        commandManager.addCommand(UserInfoCommand.class);
        commandManager.addCommand(RemoveBukkitServerCommand.class);
        commandManager.addCommand(ConnectCommand.class);
        commandManager.addCommand(SetPropertyCommand.class);
        commandManager.addCommand(FriendCommand.class);
        commandManager.addCommand(RankCommand.class);
        commandManager.addCommand(StaffChatCommand.class);
        commandManager.addCommand(HubCommand.class);
        commandManager.addCommand(TestFlowCommand.class);
        commandManager.addCommand(ImportTagsCommand.class);
        commandManager.addCommand(AllServersCommand.class);
        commandManager.addCommand(ChatCommand.class);
        commandManager.addCommand(InsertMapCommand.class);
        commandManager.addCommand(TestCommand.class);
        commandManager.addCommand(DeployTestCommand.class);
        commandManager.addCommand(UnbanCommand.class);
        commandManager.addCommand(UnmuteCommand.class);
        commandManager.addCommand(RegisterCommand.class);
        commandManager.addCommand(LoginCommand.class);
        commandManager.addCommand(LogoutCommand.class);
        commandManager.addCommand(UnregisterCommand.class);
        commandManager.addCommand(ChangePasswordCommand.class);
        commandManager.addCommand(AuthMeTransferCommand.class);

        commandManager.addCommand(new BanCommand());
        commandManager.addCommand(new MuteCommand());
        commandManager.addCommand(new BanListCommand());
        commandManager.addCommand(new MuteListCommand());

        for (GameMode gameMode : GameMode.VALUES) {
            if (!gameMode.isStaticGame()) {
                continue;
            }

            String name = gameMode.name().toLowerCase();

            if (name.contains("_")) {
                name = name.replace("_", "");
            }

            commandManager.registerCommand(name, new GameModeCommand(commandManager, name, gameMode));
        }
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

        boolean isPlayer = sender instanceof Gamer;

        String[] split = line.substring(1).split("\\s+");

        String label = split[0].toLowerCase();
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        if (isPlayer && pluginManager.callEvent(new CommandEvent((Gamer) sender, label, args, false)).isCancelled()) {
            return;
        }

        val command = getCommand(label);

        if (command != null) {
            command.execute(sender, args);
        }
    }

    public <T> void setProperty(final @NonNull String property,
                                final @NonNull T value) {
        try (val outputStream = new FileOutputStream("persistent.properties")) {
            val properties = new Properties();
            properties.setProperty(property, value.toString());
            properties.store(outputStream, null);
        } catch (final IOException e) {
            Logging.IMP.stacktrace(e, "Exception caught on data save");
        }

    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(final @NonNull String property,
                             final @Nullable T fallback) {
        try (val inputStream = new FileInputStream("persistent.properties")) {
            val properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(property) == null ? fallback : (T) properties.getProperty(property);
        } catch (final Exception e) {
            Logging.IMP.stacktrace(e, "Exception caught on data read");
            return fallback;
        }
    }

    public PluginManager pluginManager() {
        return (PluginManager) pluginManager;
    }

    // endregion

    // region outputs

    private @NotNull String coloredEpoll(final boolean used) {
        return used
                ? ChatColor.GREEN + "Used"
                : ChatColor.RED + "Not used";
    }
}
