package net.skykingdoms.backend.node.connection;

import com.mysql.cj.log.Log;
import io.netty.channel.socket.SocketChannel;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.node.NodeApp;
import net.skykingdoms.backend.node.manager.DeploymentManager;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteServerChannel;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.PacketProtocol;
import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import net.skykingdoms.backend.protocol.packet.play.*;
import net.skykingdoms.backend.protocol.type.WrappedNodeSnapshot;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentNodeDataImpl;

import java.util.EnumSet;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class RemoteServerConnection extends AbstractRemoteServerChannel {

    public RemoteServerConnection(final @NonNull LocalClientConnection local,
                                  final @NonNull SocketChannel channel) {
        super(local, channel);
    }

    @Override
    public void active() {
        channel.eventLoop().execute(() -> {
            NodeApp.INSTANCE.reloadNodeData();

            upgradeConnection(PacketProtocol.HANDSHAKE);

            EnumSet<Handshake.RequestFlag> flags = EnumSet.noneOf(Handshake.RequestFlag.class);
            if (NodeApp.INSTANCE.reconnecting()) {
                flags.add(Handshake.RequestFlag.IS_RECONNECT);
            }
            flags.add(Handshake.RequestFlag.HAS_FLUENT_DATA);

            sendPacket(
                    new Handshake.Request(
                            RemoteConnectedType.NODE,
                            NodeApp.INSTANCE.nodeConfiguration().getSecret(),
                            UUID.randomUUID(), // any value for NODE, doesn't matter
                            flags,
                            NodeApp.INSTANCE.nodeData(),
                            0
                    )
            );
        });
    }

    @Override
    public void process(S05UpdateNodeContainerInformation packet, ChannelProcessorContext ctx) {
        DeploymentManager.INSTANCE.handleContainerNameUpdate(
                packet.getTransactionId(),
                packet.getServerName().toLowerCase()
        );
    }

    @Override
    public void process(final @NonNull ServerDestroy.Request packet,
                        final @NonNull ChannelProcessorContext ctx) {
        Logging.IMP.info("[RSC/DEBUG] Received destruction packet: {} / graceful = {}",
                packet.getServerName(),
                packet.isGraceful());

        DeploymentManager.INSTANCE.handleDestructionRequest(
                packet.getServerName().toLowerCase(),
                packet.isGraceful(),
                ctx);
    }

    @Override
    public void process(NodeSnapshot.Request packet, ChannelProcessorContext ctx) {
        ctx.setResponse(new NodeSnapshot.Response(
                new WrappedNodeSnapshot(
                        NodeApp.INSTANCE.startDate(),
                        NodeApp.INSTANCE.bootElapsedEpoch(),
                        (int) Runtime.getRuntime().maxMemory(), (int) (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory()), (int) Runtime.getRuntime().freeMemory()
                )
        ));
    }

    @Override
    protected void onServerDisconnect() {
        NodeApp.INSTANCE.reconnecting(true);
    }

    @Override
    public void process(final @NonNull ServerDeploy.Request packet,
                        final @NonNull ChannelProcessorContext ctx) {
        val pending = packet.getPendingServer();

        Logging.IMP.info("[Deploy] Received {} deployment request. Transaction ID: #{} / Debug: {}",
                packet.getDeploymentTarget().name(), pending.getTransactionId(), packet.getDebugPort());

        ctx.setResponse(new ServerDeploy.Response(ServerDeploy.DeploymentResult.ACCEPTED, 90_000L));

        // Async execution, to let channel processor context finish and send response
        //noinspection Convert2Lambda (error: incompatible types: inference variable T has incompatible bounds)
        NodeApp.INSTANCE.executor().submit(new Runnable() {
            @Override
            public void run() {
                DeploymentManager.INSTANCE.handleRequest(
                        packet.getDeploymentTarget(),
                        pending,
                        packet.isTestFlow(),
                        packet.getTestFlowName(),
                        packet.getDebugPort(),
                        packet.getTestFlowId());
            }
        });
    }

    @Override
    public void process(final @NonNull Handshake.Response packet,
                        final @NonNull ChannelProcessorContext ctx) {
        if (packet.getResult() == Handshake.Result.FAILED) {
            Logging.IMP.error("[HANDSHAKE] Unable to connect to coordinator: {}", packet.getMessage());
            return;
        }

        Logging.IMP.info("Successfully connected to coordinator");

        upgradeConnection(PacketProtocol.PLAY);
    }

    @Override
    public void process(final @NonNull S04UpdateRemoteData packet,
                        final @NonNull ChannelProcessorContext ctx) {
        NodeApp.INSTANCE.nodeData((FluentNodeDataImpl) packet.getServerDataUpdate());
        Logging.IMP.info(packet.getServerDataUpdate().toString());
    }
}