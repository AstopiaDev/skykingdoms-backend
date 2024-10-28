package net.skykingdoms.backend.megamind.manager;

import com.google.common.collect.Streams;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameState;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.base.dependency.Dependency;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.lookup.LookupManager;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.megamind.server.impl.BukkitServer;
import net.skykingdoms.backend.megamind.server.impl.GameServer;
import net.skykingdoms.backend.megamind.server.impl.LobbyServer;
import net.skykingdoms.backend.megamind.server.impl.PersistentServer;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.play.S06RegisterServers;
import net.skykingdoms.backend.protocol.packet.play.S07UnregisterServer;
import net.skykingdoms.backend.protocol.type.GameBootResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
public final class ServerManager {

    // --------------------------------------------------------------
    @Dependency
    public static final ServerManager INSTANCE = new ServerManager();
    // --------------------------------------------------------------

    // Set of registered bukkit server instances and arena segments.
    Collection<BukkitServer> registeredBukkitInstances = CollectionUtil.createConcurrentSet();
    @Getter Collection<GameServer> gameServers = CollectionUtil.createConcurrentSet();
    @Getter Collection<PersistentServer> persistentServers = CollectionUtil.createConcurrentSet();
    @Getter ServerCommandMapper commandMapper = new ServerCommandMapper();
    @Getter final Map<String, CompletableFuture<AbstractPlayerHolder>> reconnectFutures = new HashMap<>();
    @Getter final Map<Object, GameBootResponse[]> pendingGameDeployments = new HashMap<>();
/*    @Getter
    TIntObjectMap<PersistentServerMeta> meta = new TIntObjectHashMap<>();*/

    public void postBootstrap() {
        /*this.meta = ServersSqlHandler.INSTANCE.loadServers().join();*/
    }

    /**
     * Basically registers a bukkit server instance. Arenas will be declared later
     */
    public BukkitServer registerBukkitServer(final @NonNull BukkitServer server) {
        if (isBukkitServerRegistered(server.getName())) {
            throw new IllegalArgumentException("This server is already registered!");
        }

        // handling reconnect shit
        addReconnectingFuture(server);

        ProxyManager.INSTANCE.sendToProxies(new S06RegisterServers(
                server.getName(),
                server.getHost().getHostString(),
                server.getHost().getPort(),
                server.getScaleType(),
                server.getGameType()));

        registeredBukkitInstances.add(server);

        LookupManager.INSTANCE.handleServerCreate(server);

        return server;
    }

    private final Predicate<GameServer> STAND_BY = s -> s.getCurrentState() == GameState.STAND_BY;

    private AbstractPlayerHolder selectServer(@NonNull Predicate<AbstractPlayerHolder> filter,
                                              @NonNull BinaryOperator<AbstractPlayerHolder> reducer,
                                              @NonNull Collection<GameType> types) {
        AbstractPlayerHolder best = null;

        Collection<AbstractScalable> servers = getServers();
        for (AbstractScalable abstractScalable : servers) {
/*
            Logging.IMP.info(abstractScalable.getName());
*/
            if (abstractScalable.getName().equalsIgnoreCase("undefined")) {
                servers.remove(abstractScalable);
            }
        }

        val shuffled = new ArrayList<>(servers);
        Collections.shuffle(shuffled);
        for (val $server : shuffled) {

            val server = (AbstractPlayerHolder) $server;
            if (server instanceof GameServer) {
                val gs = (GameServer) server;
                if (!types.contains(gs.getGameType())) {
                    continue;
                }
            }

            if (server instanceof PersistentServer) {
                val ps = (PersistentServer) server;
                if (!types.contains(ps.getGameType())) {
                    continue;
                }
            }

/*
            Logging.IMP.info(server.getName() + " is joinable " + server.canJoin());
*/

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

    public void handleStateChange(final BukkitServer parent,
                                  final int arenaId,
                                  final GameState state) {
        val name = parent.getName() + ((char) ('A' + arenaId - 1));
        val gameServer = getGameServer(name);
        if (gameServer == null) {
            Logging.IMP.warn("[ServerManager/D] No such server: {}", name);
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

    public GameServer getGameServer(final @NonNull BukkitServer parent,
                                    final int arenaId) {
        return getGameServer(parent.getName() + ((char) ('A' + arenaId - 1)));
    }

/*    public Collection<AbstractScalable> getServers() {
        val gameServers = getGameServers();
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

    public Collection<AbstractScalable> getServers() {
        val gameServers = getGameServers();
        val lobbyServers = LobbyManager.INSTANCE.getRegisteredLobbyServers();
        val persistentServers = getPersistentServers();
//        val bukkitServers = new HashSet<>(registeredBukkitInstances);
//        bukkitServers.removeIf(bs ->
//                lobbyServers.stream().anyMatch(ls -> ls.getParentServer() == bs)
//                || gameServers.stream().anyMatch(gs -> gs.getParentServer() == bs));

        return Streams.concat(gameServers.stream(), lobbyServers.stream(), persistentServers.stream())
                .collect(Collectors.toSet());
    }



    private GameServer selectArenaServer(@NonNull Predicate<GameServer> filter,
                                         @NonNull BinaryOperator<GameServer> reducer,
                                         @NonNull Collection<GameMode> minis) {
        GameServer bestServer = null;

        val serverList = gameServers.stream()
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

    // selecting server by mode with max/min online
    public AbstractPlayerHolder getBestByOnline(final @Nullable Gamer gamer,
                                                final @NonNull ScalableDataImpl type,
                                                final boolean max) {
        if (type.getServerScale() == ServerScaleType.LOBBY) {
            return getLobbyServer(type.getGameType());
        }

        GameServer gameServer = getGameServer(gamer, type.getGameMode());
        PersistentServer persistentServer = getPersistentServer(gamer, type.getGameMode());
        if (gameServer != null) {
            return gameServer;
        } else {
            return persistentServer;
        }
    }

    public boolean existsServer(final @NonNull GameType type) {
        return getServers().stream().anyMatch(s -> s.getGameType() == type);
    }

    public AbstractPlayerHolder getServerByType(final @NonNull GameType type, int maxPlayers) {
        return getServers().stream()
                .filter(s -> s.getGameType() == type && s.getPlayerIds().length < maxPlayers)
                .findFirst()
                .orElse(null);
    }

    public AbstractPlayerHolder getBestGameServerByOnlineAndMap(final @Nullable Gamer gamer,
                                                final @NonNull ScalableDataImpl type,
                                                String map,
                                                final boolean max) {
        if (map == null) {
            return getBestByOnline(gamer, type, max);
        }

        return selectArenaServer(
                STAND_BY.and(server -> server.getMap().equals(map)),
                (best, server) -> best.getPlayerIds().length > server.getPlayerIds().length ? best : server,
                Collections.singleton(type.getGameMode())
        );
    }

    // selecting server by mode with max/min online
    public AbstractPlayerHolder getBestByOnline(@NonNull GameType type, boolean max) {
        BinaryOperator<AbstractPlayerHolder> reducer = max
                ? (best, server) -> (best.getPlayerIds().length > server.getPlayerIds().length) ? best : server
                : (best, server) -> (best.getPlayerIds().length < server.getPlayerIds().length) ? best : server;

        return selectServer(server -> true, reducer, Collections.singletonList(type));
    }

    public GameServer getBestBySpace(@NonNull Collection<GameMode> minis, int space) {
        return selectArenaServer(
                STAND_BY.and(server -> server.getSpace() >= space),
                (best, server) -> best.getFillRate() > server.getFillRate() ? best : server,
                minis
        );
    }

    // Игрок прыгает в портал на лобби
    public GameServer getBestBySpace(final @NonNull GameType mode,
                                     final int space) {
        return getBestBySpace(mode.getModes(), space);
    }

    // player selected gametype but didn't select map
    public GameServer getBestBySpace(final @NonNull GameMode mini,
                                     final int space) {
        return getBestBySpace(Collections.singleton(mini), space);
    }

    // player selected specified map
    public GameServer getBestWithMap(final @NonNull GameMode mini,
                                     final @NonNull String map, int space) {
        return selectArenaServer(
                STAND_BY.and(server -> server.getSpace() >= space && map.equals(server.getMap())),
                (best, server) -> best.getPlayerIds().length > server.getPlayerIds().length ? best : server,
                Collections.singleton(mini)
        );
    }

    public LobbyServer getLobbyServer(final @NonNull GameType gameType) {
        val reducer = (BinaryOperator<AbstractPlayerHolder>) (best, server) -> (best.getPlayerIds().length > server.getPlayerIds().length) ? best : server;
        return (LobbyServer) selectServer($ -> ((AbstractScalable) $).getServerScale() == ServerScaleType.LOBBY
                && ((AbstractScalable) $).getGameType() == gameType && !$.getName().equalsIgnoreCase("undefined"), reducer, Collections.emptyList());
    }

    public GameServer getGameServer(final @Nullable Gamer gamer,
                                    final @NonNull GameMode gamemode) {
        val reducer = (BinaryOperator<AbstractPlayerHolder>) (best, server) -> (best.getPlayerIds().length > server.getPlayerIds().length) ? best : server;

        return (GameServer) selectServer(
                $ -> (((AbstractScalable) $).getServerScale() == ServerScaleType.MINI || ((AbstractScalable) $).getServerScale() == ServerScaleType.MEGA)
                        && ((AbstractScalable) $).getGameMode() == gamemode
                        && (((GameServer) $).getCurrentState() == GameState.STAND_BY || ((GameServer) $).getCurrentState() == GameState.STARTING || ((GameServer) $).getCurrentState() == GameState.GAME && gamemode.isStaticGame())
                        && !$.getName().equalsIgnoreCase("undefined")
                        && (gamer == null || Arrays.stream($.getPlayerIds()).noneMatch(s -> s == gamer.getGamerId())),
                reducer,
                Collections.singleton(GameType.getLinkedGameType(gamemode)));
    }

    public PersistentServer getPersistentServer(final @Nullable Gamer gamer, final @NonNull GameMode gameMode) {
        val reducer = (BinaryOperator<AbstractPlayerHolder>) (best, server) -> (best.getPlayerIds().length > server.getPlayerIds().length) ? best : server;
        return (PersistentServer) selectServer(
                $ -> (((AbstractScalable) $).getServerScale() == ServerScaleType.PERSISTENT)
                        && ((AbstractScalable) $).getGameMode() == gameMode
                        && !$.getName().equalsIgnoreCase("undefined")
                        && (gamer == null || Arrays.stream($.getPlayerIds()).noneMatch(s -> s == gamer.getGamerId())),
                reducer,
                Collections.singleton(GameType.getLinkedGameType(gameMode)));
    }

    public boolean isBukkitServerRegistered(final @NonNull String serverName) {
        return registeredBukkitInstances.stream().anyMatch(s -> s.getName().equalsIgnoreCase(serverName));
    }

    public boolean isGameServerRegistered(final @NonNull String serverName) {
        return gameServers.stream().anyMatch(s -> s.getName().equalsIgnoreCase(serverName));
    }

    public boolean isPersistentServerRegistered(final @NonNull String serverName) {
        return persistentServers.stream().anyMatch(s -> s.getName().equalsIgnoreCase(serverName));
    }

    public BukkitServer getBukkitServer(final @NonNull String bukkitServer) {
        return registeredBukkitInstances.stream()
                .filter(s -> s.getName().equalsIgnoreCase(bukkitServer))
                .findFirst()
                .orElse(null);
    }

    public PersistentServer getPersistentServer(final @NonNull String persistentServer) {
        return persistentServers.stream()
                .filter(s -> s.getName().equalsIgnoreCase(persistentServer))
                .findFirst()
                .orElse(null);
    }

    public GameServer getGameServer(final @NonNull String gameServerName) {
        return gameServers.stream()
                .filter(s -> s.getName().equalsIgnoreCase(gameServerName))
                .findFirst()
                .orElse(null);
    }

    public GameServer registerGameServer(final @NonNull GameServer gameServer) {
        if (isGameServerRegistered(gameServer.getName())) {
            throw new IllegalArgumentException("This server is already registered!");
        }

        gameServers.add(gameServer);

        ProxyManager.INSTANCE.sendToProxies(new S06RegisterServers(
                gameServer.getName(),
                gameServer.getParentServer().getHost().getHostString(),
                gameServer.getParentServer().getHost().getPort(),
                gameServer.getServerScale(),
                gameServer.getGameType()));

        LookupManager.INSTANCE.handleServerCreate(gameServer);

        return gameServer;
    }

    public PersistentServer registerPersistentServer(final @NonNull PersistentServer persistentServer) {
        if (isPersistentServerRegistered(persistentServer.getName())) {
            return getPersistentServers().stream().filter(a -> a.getName().equalsIgnoreCase(persistentServer.getName())).findFirst().orElse(null);
        }

        persistentServers.add(persistentServer);

        ProxyManager.INSTANCE.sendToProxies(new S06RegisterServers(
                persistentServer.getName(),
                persistentServer.getParentServer().getHost().getHostString(),
                persistentServer.getParentServer().getHost().getPort(),
                persistentServer.getServerScale(),
                persistentServer.getGameType()));

        LookupManager.INSTANCE.handleServerCreate(persistentServer);

        return persistentServer;
    }

    private void addReconnectingFuture(AbstractPlayerHolder server) {
        synchronized (reconnectFutures) {
            val serverFuture = reconnectFutures.remove(server.getName());

            if (serverFuture != null) {
                serverFuture.complete(server);
            }
        }
    }

    public void removeGameServer(final @NonNull GameServer gameServer) {

        gameServers.remove(gameServer);

        // todo: route gamers

        Logging.IMP.info("[GameServer] Removed {} game server with {} players on it",
                gameServer.getName(), gameServer.getPlayerIds().length);

        ProxyManager.INSTANCE.sendToProxies(new S07UnregisterServer(gameServer.getName()));

        LookupManager.INSTANCE.handleServerRemove(gameServer);
    }

    public void removePersistentServer(final @NonNull PersistentServer persistentServer) {
        persistentServers.remove(persistentServer);

        Logging.IMP.info("[PersistentServer] Removed {} persistent server with {} players on it", persistentServer.getName(), persistentServer.getPlayerIds().length);

        LookupManager.INSTANCE.handleServerRemove(persistentServer);
    }

    public void removeBukkitServer(final @NonNull String serverName, boolean sendToProxies, boolean force) {
        val pending = getBukkitServer(serverName);

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

        // IMPORTANT: remove bukkit server after lookup shit
        // this will fuck up "online" field in lookupserver
        // but doesnt matter cuz its BukkitServer.
        // Shit fix. TODO: rewrite this part of code
        registeredBukkitInstances.remove(pending);

        // removing this server from proxy
        if (sendToProxies) {
            ProxyManager.INSTANCE.sendToProxies(new S07UnregisterServer(serverName));
        }

        // With primary BukkitServer we also remove its "depends" (game instances)
        val minors_mmmmm = Streams.concat(LobbyManager.INSTANCE.getRegisteredLobbyServers().stream(), gameServers.stream(), persistentServers.stream())
                .filter(f -> f.getParentServer() == pending)
                .distinct()
                .collect(Collectors.toList());

        if (minors_mmmmm.size() > 0) {
            Logging.IMP.info("With this server we also remove {} servers:", minors_mmmmm.size());
            for (val m : minors_mmmmm) {

                if (sendToProxies) {
                    ProxyManager.INSTANCE.sendToProxies(new S07UnregisterServer(m.getName()));
                }

                Logging.IMP.info("- " + m.getName());

                if (m instanceof GameServer) {
                    removeGameServer((GameServer) m);
                }

                if (m instanceof LobbyServer) {
                    LobbyManager.INSTANCE.removeLobbyServer((LobbyServer) m);
                }

                if (m instanceof PersistentServer) {
                    removePersistentServer((PersistentServer) m);
                }
            }

            // todo: route gamers to fallback
        }
    }

/*    public void removePersistentServer(PersistentServer persistentServer) {
        DeploymentManager.INSTANCE.addPendingDeployment(meta.get(persistentServer.getId()), NodeManager.INSTANCE.getRegisteredNodes().stream().toList().get(0));
        persistentServers.remove(persistentServer);
    }*/

    public void sendToServers(final @NonNull Packet<?> packet) {
        registeredBukkitInstances.forEach(server -> server.getChannel().sendPacket(packet));
    }

    // TODO: rewrite to use getServers()
    public AbstractScalable getPlayableServer(final @NonNull String name) {
        return getServers().stream()
                .filter(server -> server.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Basically returns random lobby server
     */
    public AbstractPlayerHolder getFallbackServer() {
        return registeredBukkitInstances.stream()
                .filter(s -> s.getScaleType() == ServerScaleType.LOBBY)
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
