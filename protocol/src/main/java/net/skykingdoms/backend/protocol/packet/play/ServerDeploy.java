package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayClientProcessor;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import net.skykingdoms.backend.protocol.server.deploy.IDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.ProxyDeployRequest;
import org.jetbrains.annotations.NotNull;


public final class ServerDeploy {

    public ServerDeploy() {
        throw new UnsupportedOperationException();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static final class Request extends Packet.Request<PlayServerProcessor, Response> {
        /**
         * Indicates which server type we are deploying
         */
        DeploymentTarget deploymentTarget;

        /**
         * Data-class realization for specified deployment target
         */
        IDeployRequest pendingServer;

        /**
         * Flag, indicating that this is testflow deployment
         */
        boolean testFlow;

        /**
         * NULL if not a testflow request
         */
        String testFlowName;

        /**
         * Debug port for testflow thingy
         * <= 0 if not debugging
         */
        int debugPort;

        /**
         * Unique ID of testflow deployment request
         * <= 0 if not a testflow request
         */
        int testFlowId;

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayServerProcessor;
        }

        @Override
        protected void read0(@NotNull ByteBuf buf) throws Exception {
            deploymentTarget = PacketUtils.readEnum(buf, DeploymentTarget.class);
            switch (deploymentTarget) {
                case PROXY: {
                    pendingServer = new ProxyDeployRequest().deserialize(buf);
                    break;
                }
                case BUKKIT:
                    pendingServer = new BukkitDeployRequest().deserialize(buf);
                    break;
            }

            testFlow = buf.readBoolean();
            if (testFlow) {
                this.testFlowName = PacketUtils.readString(buf);
                this.debugPort = buf.readInt();
                this.testFlowId = buf.readInt();
            }
        }

        @Override
        protected void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeEnum(buf, deploymentTarget);
            pendingServer.serialize(buf);

            buf.writeBoolean(testFlow);
            if (testFlow) {
                PacketUtils.writeString(buf, testFlowName);
                buf.writeInt(debugPort);
                buf.writeInt(testFlowId);
            }
        }

        @Override
        public Class<ServerDeploy.Response> getResponse() {
            return ServerDeploy.Response.class;
        }

        @Override
        public void process(@NotNull PlayServerProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }
    }

    /**
     *
     */
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @EqualsAndHashCode(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static final class Response extends Packet.Response<PlayClientProcessor> {

        /**
         * Indicates if node accepted this deployment request and began
         * deployment process.
         */
        DeploymentResult deploymentResult;

        /**
         * Amount in milliseconds that was calculated by node driver.
         * If timeout is reached, we notify coordinator that we fucked up
         * with this deployment....
         */
        long estimatedTimeout;

        @Override
        public boolean isProcessor(PacketProcessor processor) {
            return processor instanceof PlayClientProcessor;
        }

        @Override
        protected void read0(@NotNull ByteBuf buf) throws Exception {
            deploymentResult = PacketUtils.readEnum(buf, DeploymentResult.class);
            estimatedTimeout = buf.readLong();
        }

        @Override
        protected void write0(@NotNull ByteBuf buf) throws Exception {
            PacketUtils.writeEnum(buf, deploymentResult);
            buf.writeLong(estimatedTimeout);
        }

        @Override
        public void process(@NotNull PlayClientProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
            processor.process(this, ctx);
        }
    }

    /**
     * Internal indicator that shows which game server should be deployed
     */
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    @Getter
    public enum DeploymentTarget {

        PROXY,      // bungee instance
        BUKKIT,      // virtualized lobby segment
        ;

        public static final DeploymentTarget[] VALUES = values();

    }

    /**
     * Used to indicate, if our deployment is accepted on node.
     */
    public enum DeploymentResult {
        ACCEPTED, DENIED
    }

}
