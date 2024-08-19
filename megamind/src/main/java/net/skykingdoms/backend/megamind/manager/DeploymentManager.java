package net.skykingdoms.backend.megamind.manager;

import com.google.common.collect.Streams;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.command.dependency.Dependency;
import net.skykingdoms.backend.base.util.collection.CollectionUtil;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.event.network.ProxyDeployEvent;
import net.skykingdoms.backend.megamind.manager.deployment.DeploymentResponse;
import net.skykingdoms.backend.megamind.instance.impl.*;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import net.skykingdoms.backend.protocol.packet.play.*;
import net.skykingdoms.backend.protocol.server.*;
import net.skykingdoms.backend.protocol.server.deploy.IDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.ProxyDeployRequest;
import net.skykingdoms.backend.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.protocol.type.reconnect.impl.bukkit.FluentArenaDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.bukkit.FluentBukkitDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentNodeDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentProxyDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.bukkit.FluentGameDataImpl;
import net.skykingdoms.backend.protocol.util.TextFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
/**
 * Manager that corresponds to instance deployment queue and handling
 * unexpected occurrences during deploy process (failed deployments, invalid confs etc.)
 */
public final class DeploymentManager {

    // ----------------------------------------------------------------------
    @Dependency
    public static final DeploymentManager INSTANCE = new DeploymentManager();
    // ----------------------------------------------------------------------

    private static final List<String> INSTANCE_NAMES = List.of("alpha", "beta", "gamma", "delta");
    // A set of sent non-confirmed deployments
    Collection<PendingDeploymentRequest> pendingDeployments = CollectionUtil.createConcurrentSet();

    /**
     * Selects an optimal node and sends a deployment request on it
     */
    public void sendDeploymentRequest(final @NonNull IDeployRequest request,
                                      final @NonNull ServerDeploy.DeploymentTarget target,
                                      final boolean testFlow,
                                      final @Nullable String testFlowName,
                                      final int testFlowId,
                                      final @Nullable Consumer<DeploymentResponse> resultConsumer) {
        ServerScaleType scaleType = null;

        if (request instanceof ProxyDeployRequest) {
            scaleType = ServerScaleType.UNDEFINED;
        }
        if (request instanceof BukkitDeployRequest) {
            scaleType = ((BukkitDeployRequest) request).getData().getServerScale();
        }

        val epoch = System.currentTimeMillis();

        if (scaleType == null) {
            Logging.IMP.error("[Deployment] The deployment of {} has failed due to the instance scale type being null!", request.getTransactionId());
            return;
        }

        val node = NodeManager.INSTANCE.getOptimalNodeServer(scaleType);
        try {
            int testFlowPort;
            try {
                testFlowPort = TestFlowManager.INSTANCE.getTestFlow(testFlowName).getDebugPort();
            } catch (NullPointerException exception) { testFlowPort = -1; }

            node.getChannel().awaitPacket(new ServerDeploy.Request(target, request, testFlow, testFlowName, testFlowPort, testFlowId)).thenAcceptAsync(response -> {
                val result = response.getDeploymentResult();
                if (result == ServerDeploy.DeploymentResult.ACCEPTED) {
                    if (resultConsumer != null) {
                        resultConsumer.accept(DeploymentResponse.successful(request.getTransactionId()));
                    }
                    pendingDeployments.add(new PendingDeploymentRequest(request, epoch, node, response.getEstimatedTimeout()));
                } else if (resultConsumer != null) {
                    resultConsumer.accept(DeploymentResponse.DENIED_BY_NODE);
                }

            }).get(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            if (resultConsumer != null) {
                resultConsumer.accept(DeploymentResponse.TRANSACTION_FAILED);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e); // unreachable
        }
    }

    public boolean isTransactionExists(final @NonNull UUID transactionId) {
        return getRequest(transactionId) != null;
    }

    public PendingDeploymentRequest getRequest(final @NonNull UUID transactionId) {
        return pendingDeployments.stream()
                .filter(pd -> pd.getRequest().getTransactionId().toString().equals(transactionId.toString()))
                .findFirst()
                .orElse(null);
    }

    private void _handleNodeHandshake(final @NonNull RemoteClientConnection rcc,
                                      final @NonNull Handshake.Request handshake) {
        FluentNodeDataImpl fluentNodeData = (FluentNodeDataImpl) handshake.getFluentData();

        if (handshake.isReconnect()) {
            rcc.setConnectable(
                    NodeManager.INSTANCE.registerNode(new NodeInstance(
                            fluentNodeData.getNodeName(),
                            rcc,
                            fluentNodeData.getScaleTypes(),
                            fluentNodeData.getAllocatedRamMB(),
                            fluentNodeData.getMaxRamMB()
                    ))
            );
            return;
        }

        val name = bakeNodeName();
        _reserved.add(name); // reserving shit

        val node = NodeManager.INSTANCE.registerNode(
                new NodeInstance(
                        name,
                        rcc,
                        fluentNodeData.getScaleTypes(),
                        fluentNodeData.getAllocatedRamMB(),
                        fluentNodeData.getMaxRamMB()
                )
        );
        node.getChannel().sendPacket(new S04UpdateRemoteData(
                RemoteConnectedType.NODE,
                new FluentNodeDataImpl(
                        name,
                        fluentNodeData.getScaleTypes(),
                        fluentNodeData.getAllocatedRamMB(),
                        fluentNodeData.getMaxRamMB()
                )
        ));
        _reserved.remove(name);
        rcc.setConnectable(node);
    }

    /**
     * Invoked on handshake request packet with Transaction ID
     */
    public boolean validateConnection(final @NonNull Handshake.Request handshake) {
        val transaction = handshake.getTransactionId();
        val type = handshake.getType();

        // Checks if NODE instance that deployed this instance also came online.
        if (handshake.isReconnect()) {
            if (handshake.getType() == RemoteConnectedType.PROXY
                    && !NodeManager.INSTANCE.isNodeRegistered(((FluentProxyDataImpl) handshake.getFluentData()).getNodeName())) {
                return false;
            }
        }

        if (!isTransactionExists(transaction)
                && type != RemoteConnectedType.NODE
                && !handshake.isReconnect()) {
            Logging.IMP.info("[Deploy] Declined invalid transaction #{} @ {} @ {}", transaction.toString(), type.name(), isTransactionExists(transaction));
            return false;
        } else {
            return true;
        }
    }

    private void _handleReconnecting(final @NonNull RemoteClientConnection rcc,
                                     final @NonNull Handshake.Request handshake) {

        val data = handshake.getFluentData();
        val type = handshake.getType();
        switch (type) {
            case PROXY: {
                val proxyData = (FluentProxyDataImpl) data;
                val proxyName = proxyData.getProxyName();
                val node = NodeManager.INSTANCE.getNodeServer(proxyData.getNodeName());
                rcc.setConnectable(ProxyManager.INSTANCE.registerProxyServer(new ProxyInstance(
                        proxyName,
                        node,
                        rcc,
                        InetSocketAddress.createUnresolved(proxyData.getHost(), proxyData.getPort())
                )));
                // todo: send renewed instance instances
                rcc.getConnectable().getChannel().sendPacket(new S28UpdateNetworkState(MegamindApp.INSTANCE.networkState().ordinal()));

                // Notifying internal systems that we've got a proxy deployed
                // it's required by NeoProtect module
                MegamindApp.INSTANCE.pluginManager().callEvent(new ProxyDeployEvent((ProxyInstance) rcc.getConnectable(), true));

                Logging.IMP.info("[Deploy] Reconnected proxy {}", proxyName);
                break;
            }
            case SERVER: {
                val bukkitFluentData = (FluentBukkitDataImpl) data;

                val bukkitName = bukkitFluentData.getServerName();
                val node = NodeManager.INSTANCE.getNodeServer(((FluentBukkitDataImpl) data).getNodeName());
                if (node == null) {
                    Logging.IMP.debug("Server {} tried reconnecting to node {}, but its not loaded!",
                            bukkitName, ((FluentBukkitDataImpl) data).getNodeName());
                    return;
                }

                if (bukkitFluentData instanceof FluentArenaDataImpl) {
                    FluentArenaDataImpl arenaData = (FluentArenaDataImpl) bukkitFluentData;
                    rcc.setConnectable(InstanceManager.INSTANCE.registerBukkitInstance(
                            new ArenaInstance(
                                    bukkitName,
                                    arenaData.getGameMode(),
                                    rcc,
                                    bukkitFluentData.getGameType(),
                                    node,
                                    InetSocketAddress.createUnresolved(bukkitFluentData.getHost(), bukkitFluentData.getPort()),
                                    bukkitFluentData.getServerScaleType(),
                                    arenaData.getMap(),
                                    arenaData.getGameState(),
                                    arenaData.getGameId()
                            )
                    ));
                } else if (bukkitFluentData instanceof FluentGameDataImpl) {
                    FluentGameDataImpl gameData = (FluentGameDataImpl) bukkitFluentData;
                    rcc.setConnectable(InstanceManager.INSTANCE.registerBukkitInstance(
                            new GameInstance(
                                    bukkitName,
                                    gameData.getGameMode(),
                                    rcc,
                                    bukkitFluentData.getGameType(),
                                    node,
                                    InetSocketAddress.createUnresolved(bukkitFluentData.getHost(), bukkitFluentData.getPort()),
                                    bukkitFluentData.getServerScaleType()
                            )
                    ));
                } else {
                    rcc.setConnectable(LobbyManager.INSTANCE.addLobbyServer(
                            new LobbyInstance(
                                    bukkitName,
                                    rcc,
                                    bukkitFluentData.getGameType(),
                                    node,
                                    InetSocketAddress.createUnresolved(bukkitFluentData.getHost(), bukkitFluentData.getPort()),
                                    100
                            )
                    ));
                }

                Logging.IMP.info("[Deploy] Reconnected bukkit instance {}", bukkitName);
            }
        }
    }

    /**
     * Invoked when we receive C21RegisterGameInstance
     */
    public String handleArenaCreate(final @NonNull C21RegisterArenaInstance packet,
                                    final @NonNull RemoteClientConnection rcc) {
        val bukkitServer = (BukkitInstance) rcc.getConnectable();
        val gameId = packet.getArenaId();

        // eg: mini12A / mega1E
        val gameName = getGameServerName(bukkitServer.getName(), gameId);

        val slots = packet.getSlots();
        val map = packet.getMapName();
        val data = packet.getData();

        @SuppressWarnings("DataFlowIssue")
        val gameServer = InstanceManager.INSTANCE.registerBukkitInstance(new ArenaInstance(
                gameName,
                data.getGameMode(),
                rcc,
                bukkitServer.getGameType(),
                bukkitServer.getDeploymentNode(),
                bukkitServer.getHost(),
                bukkitServer.getServerScale(),
                map,
                GameState.ENDING,
                gameId
        ));

        gameServer.setJoinable(true);
        gameServer.setSlots(slots);

        Logging.IMP.info("[Deploy] Deployed game instance {} at {} with {} slots. Map: {}",
                gameName, bukkitServer.getName(), slots, map);

        TestFlowManager.INSTANCE.handleServerAction(TestFlowManager.ServerAction.ADD_GAME,
                gameServer.getLinkedTestFlowId(), gameServer);

        return gameName;
    }

    private String getGameServerName(String serverName, int gameId) {
        String name;

        if (gameId < 27) {
            // For gameId from 1 to 26: Single letter A-Z
            return TextFormat.formatText("{}{}", serverName, (char) ('A' + gameId - 1));
        } else {
            // For gameId 27 and above
            int remainingNumber = gameId - 26; // Calculate remaining number after Z
            int romanValue = getRomanValue(remainingNumber);
            int adjustedNumber = remainingNumber - romanValue;
            String romanLetter = getRomanLetter(romanValue);

            // Format the name
            return TextFormat.formatText("{}{}{}", serverName, adjustedNumber < 1 ? "" : adjustedNumber, romanLetter + "Z");
        }
    }

    private int getRomanValue(int number) {
        // Get the highest Roman numeral value that is <= number
        if (number >= 50) return 50;
        if (number >= 10) return 10;
        if (number >= 5) return 5;
        if (number >= 1) return 1;
        return 0; // Default case
    }

    private String getRomanLetter(int value) {
        // Convert Roman numeral value to Roman numeral letter
        if (value == 50) return "L";
        if (value == 10) return "X";
        if (value == 5) return "V";
        if (value == 1) return "I";
        return ""; // Default case
    }

    public void handleLobbyBoot(final @NonNull LobbyBoot.Request request,
                                final @NonNull RemoteClientConnection rcc,
                                final @NonNull ChannelProcessorContext ctx) {
        val bukkitServer = (BukkitInstance) rcc.getConnectable();
        val gameType = bukkitServer.getGameType();

        //Logging.IMP.info("I'M HERE " + gameType.name());

        // if lobby reconnects it already has a name assigned
        val lobbyName = request.getOldName() != null ? request.getOldName() : bakeLobbyName(gameType);
        val lobbyServer = LobbyManager.INSTANCE.addLobbyServer(
                new LobbyInstance(
                        lobbyName,
                        rcc,
                        gameType,
                        bukkitServer.getDeploymentNode(),
                        bukkitServer.getHost(),
                        request.getSlots()
                )
        );
        lobbyServer.setJoinable(true);
        lobbyServer.setSlots(120);

        rcc.setConnectable(lobbyServer);
        InstanceManager.INSTANCE.removeVirtualInstance(bukkitServer.getName());
        Logging.IMP.info("[Deploy] Deployed lobby instance {} at {} with {} slots",
                lobbyName, bukkitServer.getName(), request.getSlots());

        ctx.setResponse(new LobbyBoot.Response(lobbyName));

        TestFlowManager.INSTANCE.handleServerAction(TestFlowManager.ServerAction.ADD_LOBBY,
                lobbyServer.getLinkedTestFlowId(), lobbyServer);
    }

    public void handleRemoteConnection(final @NonNull RemoteClientConnection rcc,
                                       final @NonNull Handshake.Request handshake) {

        val type = handshake.getType();
        if (type == RemoteConnectedType.NODE) {
            _handleNodeHandshake(rcc, handshake);
            return;
        }

        val transactionId = handshake.getTransactionId();

        // Handling reconnecting remotely
        if (handshake.isReconnect()) {
            _handleReconnecting(rcc, handshake);
            return;
        }

        val pendingRequest = getRequest(transactionId);
        var name = "undefined";
        val request = pendingRequest.getRequest();
        val node = pendingRequest.getTargetNode();

        if (request instanceof ProxyDeployRequest) {
            name = bakeProxyName();

            if (!handshake.hasData()) {
                Logging.IMP.error("!!! NO FLUENT DATA FROM PROXY SERVER #{} !!!", request.getTransactionId());
                return;
            }

            val data = (FluentProxyDataImpl) handshake.getFluentData();
            data.setProxyName(name);
            data.setNodeName(node.getName());

            rcc.setConnectable(ProxyManager.INSTANCE.registerProxyServer(
                    new ProxyInstance(name,
                            node,
                            rcc,
                            InetSocketAddress.createUnresolved(data.getHost(), data.getPort())
                    )
            ));

            rcc.getConnectable().getChannel().sendPacket(new S04UpdateRemoteData(
                    RemoteConnectedType.PROXY, data
            ));

            List<ServerConnectionInfo> serverConnectionInfos = InstanceManager.INSTANCE.getInstances().stream()
                    .map(server -> new ServerConnectionInfo(
                            server.getName(),
                            server.getConnection().getHost(),
                            server.getConnection().getPort(),
                            server.getServerScale(),
                            server.getGameType()
                    ))
                    .distinct()
                    .collect(Collectors.toList());

            ServerConnectionInfo[] serverConnectionInfoArray = serverConnectionInfos.toArray(new ServerConnectionInfo[0]);

            rcc.getConnectable().getChannel().sendPacket(new S28UpdateNetworkState(MegamindApp.INSTANCE.networkState().ordinal()));
            rcc.getConnectable().getChannel().sendPacket(new S06RegisterServers(serverConnectionInfoArray));

            // Notifying internal systems that we've got a proxy deployed
            // it's required by NeoProtect module
            MegamindApp.INSTANCE.pluginManager().callEvent(new ProxyDeployEvent((ProxyInstance) rcc.getConnectable(), false));

            Logging.IMP.info("[Deploy] Registered new proxy instance instance {} @ {}",
                    data.getProxyName(),
                    node.getName());
        }

        if (request instanceof BukkitDeployRequest) {
            val bukkitDeployRequest = (BukkitDeployRequest) request;
            if (!handshake.hasData()) {
                Logging.IMP.error("!!! NO FLUENT DATA FROM BUKKIT SERVER #{} !!!", bukkitDeployRequest.getTransactionId());
                return;
            }

            try {
                name = bakeBukkitName();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return;
            }

            val fluentData = (FluentBukkitDataImpl) handshake.getFluentData();
            fluentData.setNodeName(node.getName());
            fluentData.setServerName(name);

            BukkitInstance bukkitInstance = new BukkitInstance(
                    name,
                    rcc,
                    bukkitDeployRequest.getData().getGameType(),
                    bukkitDeployRequest.getData().getServerScale(),
                    node,
                    InetSocketAddress.createUnresolved(fluentData.getHost(), fluentData.getPort())
            );

            // yeah
            bukkitInstance.setLinkedTestFlowId(handshake.getTestFlowId());

            rcc.setConnectable(bukkitInstance);
            rcc.sendPacket(new S04UpdateRemoteData(RemoteConnectedType.SERVER, fluentData));

            TestFlowManager.INSTANCE.handleServerAction(TestFlowManager.ServerAction.ADD_BUKKIT,
                    handshake.getTestFlowId(), bukkitInstance);

            Logging.IMP.info("[Deploy] Registered new bukkit instance instance {} @ {} @ {} @ {}",
                    name,
                    node.getName(),
                    bukkitDeployRequest.getData().getServerScale().name(),
                    bukkitDeployRequest.getData().getGameType().name());
        }

        // removing pending deployment, because it's completed
        pendingDeployments.removeIf(pd -> pd.getRequest().getTransactionId().toString().equals(transactionId.toString()));

        if (request != null) {
            Logging.IMP.info("[Deploy] Received transaction #{} - Name: {} - Node: {} - Elapsed: {}ms.",
                    transactionId.toString(),
                    name,
                    node != null ? node.getName() : "unknown",
                    System.currentTimeMillis() - pendingRequest.getSentDeploymentAt());
        }

        if (InstanceManager.INSTANCE.getPendingGameDeployments().containsKey(transactionId)) {
            Logging.IMP.info("[Deploy] Pending game deployments contain transaction for {}! Preparing shit ...", name);
            InstanceManager.INSTANCE.updateGameQueueInfo(transactionId, name);
        }

        // Basically we update any remote deployable instance instance container's name
        if (request != null && node != null) {
            node.getChannel().sendPacket(new S05UpdateNodeContainerInformation(
                    transactionId, name
            ));
        }
    }

    /**
     * Returns a distinct set of remote connected clients, including nodes.
     */
    public @NotNull Collection<@NotNull IConnectable> getAllDeployedInstances() {
        return Streams.concat(
                        LobbyManager.INSTANCE.getLobbyInstances().stream(),
                        InstanceManager.INSTANCE.getBukkitInstances().stream(),
                        ProxyManager.INSTANCE.getProxyInstances().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Sending a packet to multiple nodes (if force) or single node (if not forced endpoint)
     *
     * @param name              name of remote instance
     * @param forceEndpoint     if we should send packet of destruction to all nodes (no instance
     *                          found by this name, but we know a container is running)
     * @param gracefulShutdown  flag that is used for shutting instance down gracefully (if not used - we destroy container)
     * @param callback          consumer that is called when
     */
    public void sendInstanceRemoveCommand(final @NonNull String name,
                                          final boolean forceEndpoint,
                                          final boolean gracefulShutdown,
                                          final Consumer<DestroyInstanceResult> callback) {

        // We know that we get not a node, which means it is deployable :3
        val remoteInstance = (IDeployable) getAllDeployedInstances().stream()
                .filter(_instance -> _instance.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (remoteInstance == null && !forceEndpoint) {
            callback.accept(DestroyInstanceResult.NO_FORCED_REMOTE);
            return;
        }

        val packet = new ServerDestroy.Request(
                name,
                gracefulShutdown
        );

        // We found exact remote client, that exist in network
        if (remoteInstance != null) {
            val nodeServer = (NodeInstance) remoteInstance.getNode();

            nodeServer.getChannel().awaitPacket(packet).thenAccept(response -> {
                if (response == null) return; // xd
                callback.accept(response.isDestroyed()
                        ? DestroyInstanceResult.DESTRUCTED_EXISTENT.ofNode(nodeServer)
                        : DestroyInstanceResult.TRANSACTION_FAILED.ofNode(nodeServer));
            });

            return;
        }

        for (val node : NodeManager.INSTANCE.getRegisteredNodes()) {
            node.getChannel().awaitPacket(packet).thenAccept(response -> {
                if (response == null || !response.isDestroyed()) return; // xd
                callback.accept(DestroyInstanceResult.FORCE_DESTRUCTED.ofNode(node));
            });
        }
    }

    /**
     * Invoked once to run ticker task for {@link #pendingDeployments} collection.
     */
    public void postBootstrap() {
        MegamindApp.INSTANCE.scheduler().scheduleAtFixedRate(() -> {
            val pendingRemoval = new HashSet<PendingDeploymentRequest>();

            for (val shit : pendingDeployments) {
                val request = shit.getRequest();
                if (shit.getSentDeploymentAt() == -1 || shit.getEstimatedTimeout() == -1) {
                    continue;
                }
                if (System.currentTimeMillis() - shit.getSentDeploymentAt() > shit.getEstimatedTimeout()) {
                    Logging.IMP.warn("[Deploy] Timeout raised for transaction #{} / Type: {}",
                            request.getTransactionId().toString(),
                            request.toString());
                    pendingRemoval.add(shit);
                }
            }

            pendingDeployments.removeAll(pendingRemoval);
        }, 0, 1, TimeUnit.MILLISECONDS); // each second we validate pending transactions
    }

    // region instance-naming

    Collection<String> _reserved = CollectionUtil.createConcurrentSet();

    // TODO: Rewrite this shitcode
    // And find a better way to do dynamic name generation
    private static final String PROXY_NAME_PATTERN = "proxy-cp-$";
    private static final String NODE_NAME_PATTERN = "a-b-c-d";

    private @NotNull String bakeNodeName() {
        String ret;
        do {
            ret = NODE_NAME_PATTERN;
            for (int i = 0; i < 4; i++) {
                ret = ret.replace(String.valueOf((char) ('a' + i)), String.valueOf(ThreadLocalRandom.current().nextInt(1, 50)));
            }
        } while (NodeManager.INSTANCE.isNodeRegistered(ret) || _reserved.contains(ret));

        return ret;
    }

    private @NotNull String bakeProxyName() {
        for (int i = 1; ; i++) {
            val proxyName = PROXY_NAME_PATTERN.replace("$", String.valueOf(i));
            if (_reserved.contains(proxyName)) continue;
            if (ProxyManager.INSTANCE.getProxy(proxyName) == null) return proxyName;
        }
    }

    private @NotNull String bakeLobbyName(final @NonNull GameType gameType) {
        val namePattern = gameType.name().toLowerCase() + "lobby$";
        for (var i = 1; ; i++) {
            val lobbyName = namePattern.replace("$", String.valueOf(i));

            if (_reserved.contains(lobbyName)) continue;

            if (LobbyManager.INSTANCE.getLobbyServer(lobbyName) == null) return lobbyName;
        }
    }

    private @NotNull String bakeBukkitName() {
        String namePattern = "instance#";
        for (int i = 1; ; i++) {
            for (String name : INSTANCE_NAMES) {
                String dynamicName = namePattern.replace("#", name + i);
                if (!InstanceManager.INSTANCE.virtualInstanceExists(dynamicName)) {
                    return dynamicName;
                }
            }
        }
    }

    // endregion

    /**
     * Basic data class that represents a "sent" deployment request
     * that is not yet being confirmed
     */
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @Data
    private static final class PendingDeploymentRequest {

        /**
         * Request instance
         */
        IDeployRequest request;

        /**
         * Sent request at (epoch ms)
         */
        long sentDeploymentAt;

        /**
         * To which node we sent this request
         */
        NodeInstance targetNode;

        /**
         * If timeout is reached we mark deployment as failed
         */
        long estimatedTimeout;

    }

    /**
     * Used in callback for instance destroy method
     */
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
    public enum DestroyInstanceResult {

        /**
         * We sent requests to every node, but none of them confirmed container destruction
         */
        NO_FORCED_REMOTE,

        /**
         * Force flag wasn't used, and we couldn't find remote instance by name
         */
        NO_UNFORCED_REMOTE,

        /**
         * Some node sent us a response that container destruction is confirmed
         */
        FORCE_DESTRUCTED,

        /**
         * A specified node sent us response confirming that we destructed a container instance
         */
        DESTRUCTED_EXISTENT,

        /**
         * Couldn't transmit a destruction request packet to remote node
         * OR if there's an exception on node driver (it can't shut shit down for some reason)
         */
        TRANSACTION_FAILED

        ;

        NodeInstance server;

        /**
         * Builder-like method to provide addition information to callback
         */
        public @NonNull DestroyInstanceResult ofNode(final @NonNull NodeInstance server) {
            this.server = server;
            return this;
        }

    }

    @AllArgsConstructor
    @Getter
    private final class WrappedPeristentDeployment {
        RemoteClientConnection cc;
        Handshake.Request request;
    }

}
