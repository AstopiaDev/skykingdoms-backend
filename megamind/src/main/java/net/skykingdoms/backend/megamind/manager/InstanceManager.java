package net.skykingdoms.backend.megamind.manager;

import com.google.common.collect.Streams;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.command.dependency.Dependency;
import net.skykingdoms.backend.base.util.collection.CollectionUtil;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.lookup.LookupManager;
import net.skykingdoms.backend.megamind.instance.AbstractPlayerHolder;
import net.skykingdoms.backend.megamind.instance.AbstractScalable;
import net.skykingdoms.backend.megamind.instance.impl.*;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.play.S06RegisterServers;
import net.skykingdoms.backend.protocol.packet.play.S07UnregisterServer;
import net.skykingdoms.backend.protocol.server.GameMode;
import net.skykingdoms.backend.protocol.server.GameState;
import net.skykingdoms.backend.protocol.server.GameType;
import net.skykingdoms.backend.protocol.server.ServerScaleType;
import net.skykingdoms.backend.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.protocol.type.GameBootResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public final class InstanceManager {

    // --------------------------------------------------------------
    @Dependency
    public static final InstanceManager INSTANCE = new InstanceManager();
    // --------------------------------------------------------------

    // Set of registered bukkit instance instances and arena segments.
    @Getter Map<String, BukkitInstance> virtualInstances = CollectionUtil.createConcurrentMap();
    @Getter Collection<BukkitInstance> bukkitInstances = CollectionUtil.createConcurrentSet();
    @Getter ServerCommandMapper commandMapper = new ServerCommandMapper();
    @Getter final Map<String, CompletableFuture<AbstractPlayerHolder>> reconnectFutures = new HashMap<>();
    @Getter final Map<Object, GameBootResponse[]> pendingGameDeployments = new HashMap<>();

    /**
     * Basically registers a dynamic bukkit instance. Arenas will be declared later
     */
    /**
     * Registers a dynamic Bukkit instance. Arenas are declared separately.
     * Depending on the type of instance (GameInstance, ArenaInstance),
     * it is added to the corresponding collections.
     *
     * @param server the BukkitInstance to register.
     * @return the registered BukkitInstance.
     * @throws IllegalArgumentException if the server is already registered.
     */
    public BukkitInstance registerBukkitInstance(@NonNull BukkitInstance server) {
        if (bukkitInstanceExists(server.getName())) {
            throw new IllegalArgumentException("This instance is already registered!");
        }

        // Handling reconnect futures
        addReconnectingFuture(server);

        // Send registration information to proxies
        ProxyManager.INSTANCE.sendToProxies(new S06RegisterServers(
                server.getName(),
                server.getHost().getHostString(),
                server.getHost().getPort(),
                server.getScaleType(),
                server.getGameType()));

        // Add the server to the appropriate collections based on its type
        bukkitInstances.add(server);

        // Notify the LookupManager about the new server creation
        LookupManager.INSTANCE.handleServerCreate(server);

        return server;
    }

    public BukkitInstance registerVirtualInstance(@NonNull BukkitInstance server) {
        if (bukkitInstanceExists(server.getName())) {
            throw new IllegalArgumentException("This instance is already registered!");
        }

        // Add the server to the appropriate collections based on its type
        virtualInstances.put(server.getName(), server);

        // Notify the LookupManager about the new server creation
        LookupManager.INSTANCE.handleServerCreate(server);

        return server;
    }

    private final Predicate<ArenaInstance> STAND_BY = s -> s.getCurrentState() == GameState.STAND_BY;

    private AbstractPlayerHolder selectServer(@NonNull Predicate<AbstractPlayerHolder> filter,
                                              @NonNull BinaryOperator<AbstractPlayerHolder> reducer,
                                              @NonNull Collection<GameType> types) {
        AbstractPlayerHolder best = null;

        Collection<AbstractScalable> servers = getInstances();
        
        val shuffled = new ArrayList<>(servers);
        Collections.shuffle(shuffled);
        for (val $server : shuffled) {

            val server = (AbstractPlayerHolder) $server;
            if (server instanceof GameInstance) {
                val gs = (GameInstance) server;
                if (!types.contains(gs.getGameType())) {
                    continue;
                }
            }

            if (!server.canJoin() || !filter.test(server)) {
                continue;
            }

            if (best == null) {
                best = server;
            } else {
                best = reducer.apply(best, server);
            }
        }

        return best;
    }

    public void handleStateChange(final BukkitInstance parent,
                                  final int arenaId,
                                  final GameState state) {
        val name = parent.getName() + ((char) ('A' + arenaId - 1));
        val gameServer = getArenaServer(name);
        if (gameServer == null) {
            Logging.IMP.warn("[ServerManager/D] No such instance: {}", name);
            return;
        }

        if (gameServer.getCurrentState() == state) {
            Logging.IMP.warn("[ServerManager/D] {} already in {}-state", name, state.name());
            return;
        }

        Logging.IMP.warn("[ServerManager/D] {} updated to {}-state", name, state.name());

        gameServer.setCurrentState(state);

        LookupManager.INSTANCE.handleStateChange(gameServer);
    }

    /**
     * Queueing deployment of game instance with specified servers
     * @param responses list of pending game deployments
     */
    public void queueGameInstanceBoot(final @NonNull UUID transactionId,
                                      final @NonNull GameBootResponse[] responses) {
        if (pendingGameDeployments.containsKey(transactionId)) {
            throw new IllegalStateException("Omg uhhhh fucked??");
        }

        pendingGameDeployments.put(transactionId, responses);
    }

    public void updateGameQueueInfo(final @NonNull UUID transactionId,
                                    final @NonNull String name) {
        if (pendingGameDeployments.containsKey(transactionId)) { // shitcode
            val value = pendingGameDeployments.get(transactionId);
            pendingGameDeployments.remove(transactionId);
            pendingGameDeployments.put(name, value);
        }
    }

    public void flushGameBoot(final @NonNull Object identify) {
        pendingGameDeployments.remove(identify);
        Logging.IMP.info("[ServerManager/D] Flushed deployment info of {}", identify);
    }

    public GameInstance getArenaServer(final @NonNull BukkitInstance parent,
                                      final int arenaId) {
        return getArenaServer(parent.getName() + ((char) ('A' + arenaId - 1)));
    }

/*    public Collection<AbstractScalable> getInstances() {
        val gameServers = getArenaServers();
        val lobbyServers = LobbyManager.INSTANCE.getRegisteredLobbyServers();
        val bukkitServers = new HashSet<>(registeredBukkitInstances);
        val persistentServers = getPersistentServers();
        bukkitServers.removeIf(bs ->
                lobbyServers.stream().anyMatch(ls -> ls.getParentServer().equals(bs))
                        || gameServers.stream().anyMatch(gs -> gs.getParentServer().equals(bs))
                        || persistentServers.stream().anyMatch(gs -> gs.getParentServer().equals(bs)));

        return Streams.concat(gameServers.stream(), lobbyServers.stream(), bukkitServers.stream(), persistentServers.stream())
                .collect(Collectors.toSet());
    }*/

    public Collection<AbstractScalable> getInstances() {
        Collection<BukkitInstance> bukkitInstances = getBukkitInstances();
        Collection<LobbyInstance> lobbyServers = LobbyManager.INSTANCE.getLobbyInstances();

        Set<AbstractScalable> combinedInstances = Stream.concat(
                bukkitInstances.stream(),
                lobbyServers.stream()
        ).collect(Collectors.toSet());

        return combinedInstances;
    }


    private ArenaInstance selectArenaServer(@NonNull Predicate<ArenaInstance> filter,
                                           @NonNull BinaryOperator<ArenaInstance> reducer,
                                           @NonNull Collection<GameMode> minis) {
        ArenaInstance bestServer = null;

        val serverList = bukkitInstances.stream()
                .map(s -> (ArenaInstance) s)
                .filter(filter)
                .filter(c -> minis.contains(c.getGameMode()))
                .collect(Collectors.toList());

        Collections.shuffle(serverList, ThreadLocalRandom.current());

        for (val server : serverList) {
            if (bestServer == null) {
                bestServer = server;
            } else {
                bestServer = reducer.apply(bestServer, server);
            }
        }

        return bestServer;
    }

    // selecting instance by mode with max/min online
    // todo: will return to this
    public AbstractPlayerHolder getBestByOnline(final @Nullable Gamer gamer,
                                                final @NonNull GameMode gameMode,
                                                final boolean max) {
        BukkitInstance instance = getArenaServer(gamer, gameMode);
        return instance;
    }

    public boolean existsServer(final @NonNull GameType type) {
        return getInstances().stream().anyMatch(s -> s.getGameType() == type);
    }

    public AbstractPlayerHolder getServerByType(final @NonNull GameType type, int maxPlayers) {
        return getInstances().stream()
                .filter(s -> s.getGameType() == type && s.getPlayerIds().length < maxPlayers)
                .findFirst()
                .orElse(null);
    }

    public AbstractPlayerHolder getBestGameServerByOnlineAndMap(final @Nullable Gamer gamer,
                                                final @NonNull ScalableDataImpl type,
                                                String map,
                                                final boolean max) {
/*        if (map == null) {
            return getBestByOnline(gamer, type, max);
        }*/

        return selectArenaServer(
                STAND_BY.and(server -> server.getMap().equals(map)),
                (best, server) -> best.getPlayerIds().length > server.getPlayerIds().length ? best : server,
                Collections.singleton(type.getGameMode())
        );
    }

    // selecting instance by mode with max/min online
    public AbstractPlayerHolder getBestByOnline(@NonNull GameType type, boolean max) {
        BinaryOperator<AbstractPlayerHolder> reducer = max
                ? (best, server) -> (best.getPlayerIds().length > server.getPlayerIds().length) ? best : server
                : (best, server) -> (best.getPlayerIds().length < server.getPlayerIds().length) ? best : server;

        return selectServer(server -> true, reducer, Collections.singletonList(type));
    }

    public GameInstance getBestBySpace(@NonNull Collection<GameMode> minis, int space) {
        return selectArenaServer(
                STAND_BY.and(server -> server.getSpace() >= space),
                (best, server) -> best.getFillRate() > server.getFillRate() ? best : server,
                minis
        );
    }

    // Игрок прыгает в портал на лобби
    public GameInstance getBestBySpace(final @NonNull GameType mode,
                                       final int space) {
        return getBestBySpace(mode.getModes(), space);
    }

    // player selected gametype but didn't select map
    public GameInstance getBestBySpace(final @NonNull GameMode mini,
                                       final int space) {
        return getBestBySpace(Collections.singleton(mini), space);
    }

    // player selected specified map
    public ArenaInstance getBestWithMap(final @NonNull GameMode mini,
                                       final @NonNull String map, int space) {
        return selectArenaServer(
                STAND_BY.and(server -> server.getSpace() >= space && map.equals(server.getMap())),
                (best, server) -> best.getPlayerIds().length > server.getPlayerIds().length ? best : server,
                Collections.singleton(mini)
        );
    }

    //TODO: MAKE THIS LOOK NICER
    public ArenaInstance getArenaServer(final @Nullable Gamer gamer,
                                      final @NonNull GameMode gamemode) {
        val reducer = (BinaryOperator<AbstractPlayerHolder>) (best, server) -> (best.getPlayerIds().length > server.getPlayerIds().length) ? best : server;

        return (ArenaInstance) selectServer(
                $ -> (((AbstractScalable) $).getServerScale() == ServerScaleType.MEDIUM || ((AbstractScalable) $).getServerScale() == ServerScaleType.LARGE)
                        && ((AbstractScalable) $).getGameMode() == gamemode
                        && (((ArenaInstance) $).getCurrentState() == GameState.STAND_BY || ((ArenaInstance) $).getCurrentState() == GameState.STARTING || ((ArenaInstance) $).getCurrentState() == GameState.GAME && gamemode.isStaticGame())
                        && !$.getName().equalsIgnoreCase("undefined")
                        && (gamer == null || Arrays.stream($.getPlayerIds()).noneMatch(s -> s == gamer.getGamerId())),
                reducer,
                Collections.singleton(GameType.getLinkedGameType(gamemode)));
    }

    public BukkitInstance getBukkitInstance(final @Nullable Gamer gamer,
                                            final @NonNull GameMode gameMode) {

        // Define a reducer to find the server with the fewest players
        BinaryOperator<AbstractPlayerHolder> findBestServer = (best, candidate) ->
                (best.getPlayerIds().length > candidate.getPlayerIds().length) ? best : candidate;

        // Criteria to select a suitable server
        Predicate<AbstractScalable> isSuitableServer = server ->
                (server.getServerScale() == ServerScaleType.MEDIUM || server.getServerScale() == ServerScaleType.LARGE) &&
                        server.getGameMode() == gameMode;

        Predicate<ArenaInstance> isValidArenaInstance = arena ->
                (arena.getCurrentState() == GameState.STAND_BY ||
                        arena.getCurrentState() == GameState.STARTING ||
                        (arena.getCurrentState() == GameState.GAME && gameMode.isStaticGame())) &&
                        !arena.getName().equalsIgnoreCase("undefined") &&
                        (gamer == null || Arrays.stream(arena.getPlayerIds()).noneMatch(playerId -> playerId == gamer.getGamerId()));

        // Select and return the best suitable server
        AbstractPlayerHolder selectedServer = selectServer(
                server -> server instanceof AbstractScalable
                        && isSuitableServer.test((AbstractScalable) server)
                        && (server instanceof ArenaInstance && isValidArenaInstance.test((ArenaInstance) server)),
                findBestServer,
                Collections.singleton(GameType.getLinkedGameType(gameMode))
        );

        return (BukkitInstance) selectedServer;
    }

    public boolean bukkitInstanceExists(final @NonNull String serverName) {
        return bukkitInstances.stream().anyMatch(s -> s.getName().equalsIgnoreCase(serverName));
    }

    public boolean virtualInstanceExists(final @NonNull String serverName) {
        return virtualInstances.containsKey(serverName);
    }

    public BukkitInstance getBukkitInstance(final @NonNull String bukkitServer) {
        return bukkitInstances.stream()
                .filter(s -> s.getName().equalsIgnoreCase(bukkitServer))
                .findFirst()
                .orElse(null);
    }

    public ArenaInstance getArenaServer(@NonNull String gameServerName) {
        return bukkitInstances.stream()
                .filter(s -> s instanceof ArenaInstance && s.getName().equalsIgnoreCase(gameServerName))
                .map(s -> (ArenaInstance) s)
                .findFirst()
                .orElse(null);
    }

    public ArenaInstance registerArenaInstance(final @NonNull ArenaInstance arenaInstance) {
        if ((arenaInstanceExists(arenaInstance.getName()))) {
            throw new IllegalArgumentException("This instance is already registered!");
        }

        bukkitInstances.add(arenaInstance);

        ProxyManager.INSTANCE.sendToProxies(new S06RegisterServers(
                arenaInstance.getName(),
                arenaInstance.getHost().getHostString(),
                arenaInstance.getHost().getPort(),
                arenaInstance.getScaleType(),
                arenaInstance.getGameType()));

        LookupManager.INSTANCE.handleServerCreate(arenaInstance);

        return arenaInstance;
    }

    public boolean arenaInstanceExists(final @NonNull String serverName) {
        return bukkitInstances.stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(serverName));
    }

    private void addReconnectingFuture(AbstractPlayerHolder server) {
        synchronized (reconnectFutures) {
            val serverFuture = reconnectFutures.remove(server.getName());

            if (serverFuture != null) {
                serverFuture.complete(server);
            }
        }
    }

    public void removeArenaInstance(final @NonNull ArenaInstance gameServer) {

        bukkitInstances.remove(gameServer);

        // todo: route gamers

        Logging.IMP.info("[GameServer] Removed {} game instance with {} players on it",
                gameServer.getName(), gameServer.getPlayerIds().length);

        ProxyManager.INSTANCE.sendToProxies(new S07UnregisterServer(gameServer.getName()));

        LookupManager.INSTANCE.handleServerRemove(gameServer);
    }

    public void removeBukkitInstance(final @NonNull String serverName, boolean sendToProxies, boolean force) {
        val pending = getBukkitInstance(serverName);

        if (pending == null) {
            if (force) {
                if (sendToProxies) {
                    ProxyManager.INSTANCE.sendToProxies(new S07UnregisterServer(serverName));
                }
            }
            return;
        }

        Logging.IMP.info("[Bukkit] Removed {} instance", pending.getName());

        // Removing shit from command mapper (we don't need it anymore)
        commandMapper.flushServerCommands(pending.getName());

        // removing shit from lookups
        LookupManager.INSTANCE.handleServerRemove(pending);

        // IMPORTANT: remove bukkit instance after lookup shit
        // this will fuck up "online" field in lookupserver
        // but doesnt matter cuz its BukkitServer.
        // Shit fix. TODO: rewrite this part of code
        bukkitInstances.remove(pending);

        // removing this instance from proxy
        if (sendToProxies) {
            ProxyManager.INSTANCE.sendToProxies(new S07UnregisterServer(serverName));
            // todo: route gamers to fallback
        }
    }

    public void removeVirtualInstance(final @NonNull String instanceName) {
        virtualInstances.remove(instanceName);
    }

/*    public void removePersistentServer(PersistentServer persistentServer) {
        DeploymentManager.INSTANCE.addPendingDeployment(meta.get(persistentServer.getId()), NodeManager.INSTANCE.getRegisteredNodes().stream().toList().get(0));
        persistentServers.remove(persistentServer);
    }*/

    public void sendToServers(final @NonNull Packet<?> packet) {
        Stream.concat(
                bukkitInstances.stream(),
                LobbyManager.INSTANCE.getLobbyInstances().stream()
        ).forEach(server -> server.getChannel().sendPacket(packet));
    }

    // TODO: rewrite to use getInstances()
    public AbstractScalable getPlayableServer(final @NonNull String name) {
        return getInstances().stream()
                .filter(server -> {
                    return server.getName().equals(name);
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Basically returns random lobby instance
     */
    public AbstractPlayerHolder getFallbackServer() {
        return bukkitInstances.stream()
                .filter(s -> s instanceof LobbyInstance)
                .findFirst()
                .orElse(null);
    }


    public CompletableFuture<AbstractPlayerHolder> getFutureServer(@NotNull String name) {
        AbstractPlayerHolder server = getPlayableServer(name);

        if (server != null) {
            return CompletableFuture.completedFuture(server);
        }

        synchronized (reconnectFutures) {
            return reconnectFutures.computeIfAbsent(name.toLowerCase(), x -> new CompletableFuture<>());
        }
    }

}
