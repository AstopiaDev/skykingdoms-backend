package net.skykingdoms.backend.megamind.manager;

import com.google.common.collect.Streams;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameState;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.api.protocol.util.TextFormat;
import net.skykingdoms.backend.base.data.network.PersistentServerMeta;
import net.skykingdoms.backend.base.dependency.Dependency;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.event.network.ProxyDeployEvent;
import net.skykingdoms.backend.megamind.manager.deployment.DeploymentResponse;
import net.skykingdoms.backend.megamind.server.impl.*;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import net.skykingdoms.backend.protocol.packet.play.*;
import net.skykingdoms.backend.protocol.server.IConnectable;
import net.skykingdoms.backend.protocol.server.IDeployable;
import net.skykingdoms.backend.protocol.server.ServerConnectionInfo;
import net.skykingdoms.backend.protocol.server.deploy.IDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.PersistentBukkitDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.ProxyDeployRequest;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentNodeDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentProxyDataImpl;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentServiceDataImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
/**
 * Manager that corresponds to server deployment queue and handling
 * unexpected occurrences during deploy process (failed deployments, invalid confs etc.)
 */
public final class DeploymentManager {

    // ----------------------------------------------------------------------
    @Dependency
    public static final DeploymentManager INSTANCE = new DeploymentManager();
    // ----------------------------------------------------------------------

    // A set of sent non-confirmed deployments
    Collection<PendingDeploymentRequest> pendingDeployments = CollectionUtil.createConcurrentSet();
    Queue<WrappedPeristentDeployment> requests = new ConcurrentLinkedQueue<>();

    /**
     * Selects an optimal node and sends a deployment request on it
     */
    public void sendDeploymentRequest(final @NonNull IDeployRequest request,
                                      final @NonNull ServerDeploy.DeploymentTarget target,
                                      final boolean testFlow,
                                      final @Nullable String testFlowName,
                                      final int testFlowId,
                                      final @Nullable Consumer<DeploymentResponse> resultConsumer) {
        val nodes = NodeManager.INSTANCE.getRegisteredNodes();
        val epoch = System.currentTimeMillis();

        // TODO: NODES PRIORITY SISTEM

        try {
            val node = NodeManager.INSTANCE.getNodeServerFromAddress("5.9.150.40");

            int testFlowPort;
            try {
                testFlowPort = TestFlowManager.INSTANCE.getTestFlow(testFlowName).getDebugPort();
            } catch (NullPointerException exception) { testFlowPort = -1; }

            node.getChannel().awaitPacket(new ServerDeploy.Request(target, request, testFlow, testFlowName, testFlowPort, testFlowId)).thenAccept(response -> {
                val result = response.getDeploymentResult();

                //Logging.IMP.info("[Deploy] Deployment request #{}, result: {}", request.getTransactionId().toString(), result.name());

                if (result == ServerDeploy.DeploymentResult.ACCEPTED) {
                    Logging.IMP.info("Deployment accepted!");
                    if (resultConsumer != null) {
                        resultConsumer.accept(DeploymentResponse.successful(request.getTransactionId()));
                    }

                    /*Logging.IMP.info("[Deploy] Deployment request #{} accepted by node {}",
                            request.getTransactionId().toString(),
                            node.getName());*/
                    pendingDeployments.add(new PendingDeploymentRequest(request, epoch, node, response.getEstimatedTimeout()));
                } else if (resultConsumer != null) {
                    resultConsumer.accept(DeploymentResponse.DENIED_BY_NODE);
                }

            }).get(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            /*Logging.IMP.info("[Deploy] Deployment request #{} failed due to timeout",
                    request.getTransactionId().toString());*/
            if (resultConsumer != null) {
                resultConsumer.accept(DeploymentResponse.TRANSACTION_FAILED);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e); // unreachable
        }
    }

    public void addPendingDeployment(PersistentServerMeta meta, NodeServer nodeServer) {
        Logging.IMP.info(meta.getTransactionId().toString());
        pendingDeployments.add(new DeploymentManager.PendingDeploymentRequest(
                new BukkitDeployRequest(meta.getTransactionId(), meta.getGameMode().bakeData()),
                -1,
                nodeServer,
                -1
        ));
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

        if (handshake.isReconnect()) {
            rcc.setConnectable(NodeManager.INSTANCE.registerNode(new NodeServer(((FluentNodeDataImpl) handshake.getFluentData()).getNodeName(), rcc)));
            return;
        }

        val name = bakeNodeName();
        _reserved.add(name); // reserving shit

        val node = NodeManager.INSTANCE.registerNode(new NodeServer(name, rcc));
        val currentData = (FluentNodeDataImpl) handshake.getFluentData();

        currentData.setNodeName(name);

        node.getChannel().sendPacket(new S04UpdateRemoteData(RemoteConnectedType.NODE, currentData));

        _reserved.remove(name); // invalidation reservation record

        rcc.setConnectable(node);

        if (!requests.isEmpty()) {
            for (WrappedPeristentDeployment deployment : requests) {
                val fluentData = (FluentBukkitDataImpl) deployment.request.getFluentData();
                if (node.getConnection().getHost().equalsIgnoreCase(fluentData.getHost())) {
                    handleRemoteConnection(deployment.cc, deployment.request);
                }
            }
        }
    }

    private void _handleServiceHandshake(final @NonNull RemoteClientConnection rcc,
                                      final @NonNull Handshake.Request handshake) {
        val fluentData = (FluentServiceDataImpl) handshake.getFluentData();
        val service = ServiceManager.INSTANCE.registerService(
                new ServiceServer(
                        fluentData.getServiceName(),
                        rcc)
        );

        MegamindApp.INSTANCE.pluginManager().registerListener(fluentData.getServiceName(), fluentData.getEventSubscriptions());
        rcc.setConnectable(service);
    }

    /**
     * Invoked on handshake request packet with Transaction ID
     */
    public boolean validateConnection(final @NonNull Handshake.Request handshake) {
        val transaction = handshake.getTransactionId();
        val type = handshake.getType();

/*
        Logging.IMP.info("[Deploy/Info] {}, {}, {}, {}", handshake.getType(), handshake.getFluentData() instanceof FluentBukkitDataImpl ? " yes" : " no", handshake.hasData(), handshake.getFluentData().toString());
*/


        if (handshake.getFluentData() instanceof FluentBukkitDataImpl) {
            FluentBukkitDataImpl data = (FluentBukkitDataImpl) handshake.getFluentData();
            if (data.getServerScaleType() == ServerScaleType.PERSISTENT) {
                NodeServer nodeServer = NodeManager.INSTANCE.getNodeServerFromAddress(data.getHost());
                return nodeServer != null;

/*                if (isTransactionExists(transaction)) {
                    return true;
                } else {
                    Logging.IMP.info("[Deploy] Declined invalid transaction #{} @ {} @ {}", transaction.toString(), type.name(), isTransactionExists(transaction));
                    return false;
                }*/
            }

            if (handshake.isReconnect()) {
                return NodeManager.INSTANCE.isNodeRegistered(data.getNodeName());
            }
        }

        // Checks if NODE server that deployed this server also came online.
        if (handshake.isReconnect()) {
            Logging.IMP.info("it is reconnect");
            if (handshake.getType() == RemoteConnectedType.PROXY
                    && !NodeManager.INSTANCE.isNodeRegistered(((FluentProxyDataImpl) handshake.getFluentData()).getNodeName())) {
                /*Logging.IMP.info("[Deploy] Denied: RCON: "
                        + handshake.isReconnect()
                        + ", T: "
                        + handshake.getType().name()
                        + ", N: "
                        + ((FluentProxyDataImpl) handshake.getFluentData()).getNodeName()
                        + ", TID: " + transaction.toString() + ", NREG: " + NodeManager.INSTANCE.isNodeRegistered(((FluentProxyDataImpl) handshake.getFluentData()).getNodeName()));*/
                return false;
            }
        }

        if (!isTransactionExists(transaction)
                && type != RemoteConnectedType.NODE
                && type != RemoteConnectedType.SERVICE
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
                rcc.setConnectable(ProxyManager.INSTANCE.registerProxyServer(new ProxyServer(
                        proxyName,
                        node,
                        rcc,
                        InetSocketAddress.createUnresolved(proxyData.getHost(), proxyData.getPort())
                )));
                // todo: send renewed server instances
                rcc.getConnectable().getChannel().sendPacket(new S28UpdateNetworkState(MegamindApp.INSTANCE.networkState().ordinal()));

                // TODO: do the command shit differently
                List<S10RegisterCommands.CommandContainer> containers = new ArrayList<>();
                for (val entry : ServerManager.INSTANCE.getCommandMapper().getCommandsEntry()) {
                    containers.add(new S10RegisterCommands.CommandContainer(
                            entry.getValue(), S10RegisterCommands.CommandSource.SERVER, entry.getKey()
                    ));
                }

                containers.add(S10RegisterCommands.CommandContainer.coreSingleton(MegamindApp.INSTANCE.getCommands())[0]);

                rcc.getConnectable().getChannel().sendPacket(new S10RegisterCommands(containers.toArray(S10RegisterCommands.CommandContainer[]::new)));

                // Notifying internal systems that we've got a proxy deployed
                // it's required by NeoProtect module
                MegamindApp.INSTANCE.pluginManager().callEvent(new ProxyDeployEvent((ProxyServer) rcc.getConnectable(), true));

                Logging.IMP.info("[Deploy] Reconnected proxy {}", proxyName);
                break;
            }
            case SERVER: {
                val bukkitFluentData = (FluentBukkitDataImpl) data;

/*                if (bukkitFluentData.getServerScaleType() == ServerScaleType.PERSISTENT) {
                    return;
                }*/

                val bukkitName = bukkitFluentData.getServerName();

                NodeServer node;
                if (bukkitFluentData.getServerScaleType() == ServerScaleType.PERSISTENT) {
                    node = NodeManager.INSTANCE.getNodeServerFromAddress(((FluentBukkitDataImpl) data).getHost());
                } else {
                    node = NodeManager.INSTANCE.getNodeServer(((FluentBukkitDataImpl) data).getNodeName());
                }

                if (node == null) {
                    Logging.IMP.info("Server {} tried reconnecting to node {}, but its not loaded!",
                            bukkitName, ((FluentBukkitDataImpl) data).getNodeName());
                    return;
                }

                rcc.setConnectable(ServerManager.INSTANCE.registerBukkitServer(new BukkitServer(
                        bukkitName,
                        rcc,
                        bukkitFluentData.getGameType(),
                        bukkitFluentData.getServerScaleType(),
                        node,
                        InetSocketAddress.createUnresolved(bukkitFluentData.getHost(), bukkitFluentData.getPort())
                )));

                Logging.IMP.info("[Deploy] Reconnected bukkit instance {}", bukkitName);
            }
        }
    }

    /**
     * Invoked when we receive C21RegisterGameInstance
     */
    public String handleGameCreate(final @NonNull C21RegisterGameInstance packet,
                                 final @NonNull RemoteClientConnection rcc) {
        val bukkitServer = (BukkitServer) rcc.getConnectable();
        val gameId = packet.getArenaId();

        // eg: mini12A / mega1E
        val gameName = getGameServerName(bukkitServer.getName(), gameId);

        val slots = packet.getSlots();
        val map = packet.getMapName();
        val data = packet.getData();

        @SuppressWarnings("DataFlowIssue")
        val gameServer = ServerManager.INSTANCE.registerGameServer(new GameServer(
                gameName,
                data.getGameMode(),
                rcc,
                bukkitServer,
                map,
                GameState.ENDING,
                gameId));

        gameServer.setJoinable(true);
        gameServer.setSlots(slots);

        Logging.IMP.info("[Deploy] Deployed game instance {} at {} with {} slots. Map: {}",
                gameName, bukkitServer.getName(), slots, map);

        // handling shit for testflow
        TestFlowManager.INSTANCE.handleServerAction(TestFlowManager.ServerAction.ADD_GAME,
                gameServer.getParentServer().getLinkedTestFlowId(), gameServer);

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


    /**
     * Invoked when we receive RegisterPersistentInstance
     */
    public void handlePersistentCreate(final @NonNull C29RegisterPersistentInstance packet,
                                       final @NonNull RemoteClientConnection rcc) {
        val bukkitServer = (BukkitServer) rcc.getConnectable();

        val data = packet.getData();
        val slots = packet.getSlots();
        val gameName = "p-" + bukkitServer.getName();

        if (ServerManager.INSTANCE.isPersistentServerRegistered(gameName)) {
            return;
        }

        @SuppressWarnings("DataFlowIssue")
        val persistentServer = ServerManager.INSTANCE.registerPersistentServer(new PersistentServer(
                gameName,
                data.getGameMode(),
                data.getGameType(),
                rcc,
                bukkitServer
        ));

        persistentServer.setJoinable(true);
        persistentServer.setSlots(slots);

        Logging.IMP.info("[Deploy] Deployed persistent instance {} at {} with {} slots. GameMode: {}",
                gameName, bukkitServer.getName(), slots, data.getGameMode().name());
    }

    public void handleLobbyBoot(final @NonNull LobbyBoot.Request request,
                                final @NonNull RemoteClientConnection rcc,
                                final @NonNull ChannelProcessorContext ctx) {
        val bukkitServer = (BukkitServer) rcc.getConnectable();
        val gameType = bukkitServer.getGameType();

        //Logging.IMP.info("I'M HERE " + gameType.name());

        // if lobby reconnects it already has a name assigned
        val lobbyName = request.getOldName() != null ? request.getOldName() : bakeLobbyName(gameType);

        val lobbyServer = LobbyManager.INSTANCE.addLobbyServer(new LobbyServer(lobbyName, rcc, bukkitServer, request.getSlots()));
        lobbyServer.setJoinable(true);
        lobbyServer.setSlots(120); // hardcoded. todo: dynamic slots amount

        Logging.IMP.info("[Deploy] Deployed lobby server {} at {} with {} slots",
                lobbyName, bukkitServer.getName(), request.getSlots());

        ctx.setResponse(new LobbyBoot.Response(lobbyName));

        TestFlowManager.INSTANCE.handleServerAction(TestFlowManager.ServerAction.ADD_LOBBY,
                lobbyServer.getParentServer().getLinkedTestFlowId(), lobbyServer);
    }

    public void handleRemoteConnection(final @NonNull RemoteClientConnection rcc,
                                       final @NonNull Handshake.Request handshake) {

        val type = handshake.getType();

        switch (type) {
            case NODE:
                _handleNodeHandshake(rcc, handshake);
                return;
            case SERVICE:
                _handleServiceHandshake(rcc, handshake);
                return;
        }

        val transactionId = handshake.getTransactionId();

        // Handling reconnecting remotely
        if (handshake.isReconnect()) {
            _handleReconnecting(rcc, handshake);
            return;
        }

        val request = getRequest(transactionId);
        var name = "undefined";

        // proxy has no testflow thing
        if (request != null && request.getRequest() instanceof ProxyDeployRequest) {
            name = bakeProxyName();
            if (!handshake.hasData()) {
                Logging.IMP.error("!!! NO FLUENT DATA FROM PROXY SERVER #{} !!!", request.getRequest().getTransactionId());
                return;
            }

            val data = (FluentProxyDataImpl) handshake.getFluentData();
            data.setProxyName(name);
            data.setNodeName(request.getTargetNode().getName());

            rcc.setConnectable(ProxyManager.INSTANCE.registerProxyServer(
                    new ProxyServer(name,
                            request.getTargetNode(),
                            rcc,
                            InetSocketAddress.createUnresolved(data.getHost(), data.getPort())
                    )
            ));

            rcc.getConnectable().getChannel().sendPacket(new S04UpdateRemoteData(
                    RemoteConnectedType.PROXY, data
            ));

            val serverConnectionInfos = new java.util.ArrayList<>(ServerManager.INSTANCE.getRegisteredBukkitInstances().stream()
                    .map(server -> new ServerConnectionInfo(
                            server.getName(),
                            server.getHost().getHostName(),
                            server.getHost().getPort(),
                            server.getScaleType(),
                            server.getGameType())).toList());

            serverConnectionInfos.addAll(
                    ServerManager.INSTANCE.getPersistentServers().stream().map(
                            server -> new ServerConnectionInfo(
                                    server.getName(),
                                    server.getParentServer().getHost().getHostName(),
                                    server.getParentServer().getHost().getPort(),
                                    server.getServerScale(),
                                    server.getGameType()
                            )).toList()
            );

            val serverConnectionInfoArray = serverConnectionInfos.toArray(ServerConnectionInfo[]::new);


            rcc.getConnectable().getChannel().sendPacket(new S28UpdateNetworkState(MegamindApp.INSTANCE.networkState().ordinal()));
            rcc.getConnectable().getChannel().sendPacket(new S06RegisterServers(serverConnectionInfoArray));

            List<S10RegisterCommands.CommandContainer> containers = new ArrayList<>();
            for (val entry : ServerManager.INSTANCE.getCommandMapper().getCommandsEntry()) {
                containers.add(new S10RegisterCommands.CommandContainer(
                        entry.getValue(), S10RegisterCommands.CommandSource.SERVER, entry.getKey()
                ));
            }

            containers.add(S10RegisterCommands.CommandContainer.coreSingleton(MegamindApp.INSTANCE.getCommands())[0]);

            rcc.getConnectable().getChannel().sendPacket(new S10RegisterCommands(containers.toArray(S10RegisterCommands.CommandContainer[]::new)));

            // Notifying internal systems that we've got a proxy deployed
            // it's required by NeoProtect module
            MegamindApp.INSTANCE.pluginManager().callEvent(new ProxyDeployEvent((ProxyServer) rcc.getConnectable(), false));

            Logging.IMP.info("[Deploy] Registered new proxy server instance {} @ {}",
                    data.getProxyName(),
                    request.getTargetNode().getName());
        }

        if (request != null && request.getRequest() instanceof BukkitDeployRequest) {
            val bukkitDeployRequest = (BukkitDeployRequest) request.getRequest();
            if (!handshake.hasData()) {
                Logging.IMP.error("!!! NO FLUENT DATA FROM BUKKIT SERVER #{} !!!", bukkitDeployRequest.getTransactionId());
                return;
            }

            try {
                name = bakeBukkitName(bukkitDeployRequest.getData().getServerScale());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return;
            }

            val fluentData = (FluentBukkitDataImpl) handshake.getFluentData();
            fluentData.setNodeName(request.getTargetNode().getName());
            fluentData.setServerName(name);

            val bukkitServer = ServerManager.INSTANCE.registerBukkitServer(new BukkitServer(
                    name,
                    rcc,
                    bukkitDeployRequest.getData().getGameType(),
                    bukkitDeployRequest.getData().getServerScale(),
                    request.getTargetNode(),
                    InetSocketAddress.createUnresolved(fluentData.getHost(), fluentData.getPort())
            ));

            // yeah
            bukkitServer.setLinkedTestFlowId(handshake.getTestFlowId());

            rcc.setConnectable(bukkitServer);

            rcc.sendPacket(new S04UpdateRemoteData(RemoteConnectedType.SERVER, fluentData));

            TestFlowManager.INSTANCE.handleServerAction(TestFlowManager.ServerAction.ADD_BUKKIT,
                    handshake.getTestFlowId(), bukkitServer);

            Logging.IMP.info("[TestFlow/Id] Testflow " + handshake.getTestFlowId());

            Logging.IMP.info("[Deploy] Registered new bukkit server instance {} @ {} @ {} @ {}",
                    name,
                    request.getTargetNode().getName(),
                    bukkitDeployRequest.getData().getServerScale().name(),
                    bukkitDeployRequest.getData().getGameType().name());
        }

        if (request != null && request.getRequest() instanceof PersistentBukkitDeployRequest) {
            val persistentBukkitDeployRequest = (PersistentBukkitDeployRequest) request.getRequest();
            if (!handshake.hasData()) {
                Logging.IMP.error("!!! NO FLUENT DATA FROM BUKKIT SERVER #{} !!!", persistentBukkitDeployRequest.getTransactionId());
                return;
            }

            try {
                name = persistentBukkitDeployRequest.getName();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return;
            }

            val fluentData = (FluentBukkitDataImpl) handshake.getFluentData();
            NodeServer server = NodeManager.INSTANCE.getNodeServerFromAddress(fluentData.getHost());

            if (server == null) {
                requests.add(new WrappedPeristentDeployment(
                        rcc,
                        handshake
                ));
                return;
            }

            fluentData.setServerName(name);
            fluentData.setNodeName(server.getName());

            val bukkitServer = ServerManager.INSTANCE.registerBukkitServer(new BukkitServer(
                    name,
                    rcc,
                    persistentBukkitDeployRequest.getData().getGameType(),
                    persistentBukkitDeployRequest.getData().getServerScale(),
                    server,
                    InetSocketAddress.createUnresolved(fluentData.getHost(), fluentData.getPort())
            ));

            // yeah
            bukkitServer.setLinkedTestFlowId(handshake.getTestFlowId());

            rcc.setConnectable(bukkitServer);

            rcc.sendPacket(new S04UpdateRemoteData(RemoteConnectedType.SERVER, fluentData));

            TestFlowManager.INSTANCE.handleServerAction(TestFlowManager.ServerAction.ADD_BUKKIT,
                    handshake.getTestFlowId(), bukkitServer);

            Logging.IMP.info("[Deploy] Registered new persistent bukkit server instance {} @ {} @ {} @ {}",
                    name,
                    fluentData.getNodeName(),
                    persistentBukkitDeployRequest.getData().getServerScale().name(),
                    persistentBukkitDeployRequest.getData().getGameType().name());

        }

        if (request == null) {
            val fluentData = (FluentBukkitDataImpl) handshake.getFluentData();
            name = fluentData.getServerName();
            val bukkitServer = ServerManager.INSTANCE.registerBukkitServer(new BukkitServer(
                    name,
                    rcc,
                    fluentData.getGameType(),
                    fluentData.getServerScaleType(),
                    null,
                    InetSocketAddress.createUnresolved(fluentData.getHost(), fluentData.getPort())
            ));

            // yeah
            bukkitServer.setLinkedTestFlowId(handshake.getTestFlowId());

            rcc.setConnectable(bukkitServer);

            rcc.sendPacket(new S04UpdateRemoteData(RemoteConnectedType.SERVER, fluentData));

            TestFlowManager.INSTANCE.handleServerAction(TestFlowManager.ServerAction.ADD_BUKKIT,
                    handshake.getTestFlowId(), bukkitServer);

            Logging.IMP.info("[Deploy] Registered new bukkit server instance {} @ {} @ {} @ {}",
                    name,
                    "none",
                    fluentData.getServerScaleType().name(),
                    fluentData.getGameType().name());
        }

        // removing pending deployment, because it's completed
        pendingDeployments.removeIf(pd -> pd.getRequest().getTransactionId().toString().equals(transactionId.toString()) && !(pd.request instanceof PersistentBukkitDeployRequest));

        if (request != null) {
            Logging.IMP.info("[Deploy] Received transaction #{} - Name: {} - Node: {} - Elapsed: {}ms.",
                    transactionId.toString(),
                    name,
                    request.getTargetNode() != null ? request.getTargetNode().getName() : "unknown",
                    System.currentTimeMillis() - request.getSentDeploymentAt());
        }

        if (ServerManager.INSTANCE.getPendingGameDeployments().containsKey(transactionId)) {
            Logging.IMP.info("[Deploy] Pending game deployments contain transaction for {}! Preparing shit ...", name);
            ServerManager.INSTANCE.updateGameQueueInfo(transactionId, name);
        }

        // Basically we update any remote deployable server instance container's name
        if (request != null && request.getTargetNode() != null) {
            request.getTargetNode().getChannel().sendPacket(new S05UpdateNodeContainerInformation(
                    transactionId, name
            ));
        }
    }

    /**
     * Returns a distinct set of remote connected clients, including nodes.
     */
    public @NotNull Collection<@NotNull IConnectable> getAllDeployedInstances() {
        return Streams.concat(
                        ServerManager.INSTANCE.getPersistentServers().stream(),
                        ServerManager.INSTANCE.getRegisteredBukkitInstances().stream(),
                        ProxyManager.INSTANCE.getProxyServers().values().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Sending a packet to multiple nodes (if force) or single node (if not forced endpoint)
     *
     * @param name              name of remote instance
     * @param forceEndpoint     if we should send packet of destruction to all nodes (no server
     *                          found by this name, but we know a container is running)
     * @param gracefulShutdown  flag that is used for shutting server down gracefully (if not used - we destroy container)
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
            val nodeServer = (NodeServer) remoteInstance.getNode();

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

    public void addPersistentDeployment(String serverName, UUID tid, String address, int port, GameType gameType, GameMode gameMode) {
        pendingDeployments.add(new PendingDeploymentRequest(
                new PersistentBukkitDeployRequest(tid, new ScalableDataImpl(
                        ServerScaleType.PERSISTENT,
                        gameType,
                        gameMode
                ), serverName, address, port),
                -1,
                null,
                -1
        ));
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

    // region server-naming

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
        val networkType = MegamindApp.INSTANCE.networkType();

        for (int i = 1; ; i++) {
            val proxyName = networkType.getPrefix() + "-" + PROXY_NAME_PATTERN.replace("$", String.valueOf(i));
            if (_reserved.contains(proxyName)) continue;
            if (ProxyManager.INSTANCE.getProxy(proxyName) == null) return proxyName;
        }
    }

    private @NotNull String bakeLobbyName(final @NonNull GameType gameType) {
        val networkType = MegamindApp.INSTANCE.networkType();
        val namePattern = gameType.name().toLowerCase() + "lobby$";

        for (var i = 1; ; i++) {
            val lobbyName = networkType.getPrefix() + "-" +  namePattern.replace("$", String.valueOf(i));

            if (_reserved.contains(lobbyName)) continue;

            if (LobbyManager.INSTANCE.getLobbyServer(lobbyName) == null) return lobbyName;
        }
    }

    private @NotNull String bakeBukkitName(final @NonNull ServerScaleType scaleType) {
        val networkType = MegamindApp.INSTANCE.networkType();

        if (scaleType == ServerScaleType.LOBBY) {
            val namePattern = "dynamiclobby#$";
            char maxDynamicChar = 'D';
            for (var i = 1; ; i++) {
                var dynamicLobbyName = networkType.getPrefix() + "-" + namePattern.replace("#", String.valueOf(i));
                for (var c = 'A'; c < maxDynamicChar; c++) {
                    dynamicLobbyName = dynamicLobbyName.replace('$', c);
                    if (ServerManager.INSTANCE.getBukkitServer(dynamicLobbyName) == null) {
                        return dynamicLobbyName;
                    }
                }
            }
        }

        if (scaleType == ServerScaleType.UNDEFINED) {
            return networkType.getPrefix() + "-" + "dynamicinstance" + ThreadLocalRandom.current().nextInt(0, 100) + ('A' + ThreadLocalRandom.current().nextInt(0, 25));
        }

        if (scaleType == ServerScaleType.MEGA || scaleType == ServerScaleType.MINI) {
            String ret;
            int i = 1;
            do {
                ret = scaleType.name().toLowerCase() + i;   // doing +1 since if there are nos servers it will return 0,
                // and we will generate name like: mini0/mega0
                i++;
            } while (ServerManager.INSTANCE.isBukkitServerRegistered(ret) || _reserved.contains(ret));

            return networkType.getPrefix() + "-" + ret;
        }

        if (scaleType == ServerScaleType.PERSISTENT) {
            String ret;
            int i = 1;
            do {
                ret = "P" + i;   // doing +1 since if there are nos servers it will return 0,
                // and we will generate name like: mini0/mega0
                i++;
            } while (ServerManager.INSTANCE.isBukkitServerRegistered(ret) || _reserved.contains(ret));

            return networkType.getPrefix() + "-" + ret;
        }

        return "undefined";
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
        NodeServer targetNode;

        /**
         * If timeout is reached we mark deployment as failed
         */
        long estimatedTimeout;

    }

    /**
     * Used in callback for server destroy method
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

        NodeServer server;

        /**
         * Builder-like method to provide addition information to callback
         */
        public @NonNull DestroyInstanceResult ofNode(final @NonNull NodeServer server) {
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
