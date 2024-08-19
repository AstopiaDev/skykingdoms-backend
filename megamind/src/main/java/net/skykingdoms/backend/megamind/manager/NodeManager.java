package net.skykingdoms.backend.megamind.manager;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.command.dependency.Dependency;
import net.skykingdoms.backend.base.util.collection.CollectionUtil;
import net.skykingdoms.backend.megamind.instance.impl.NodeInstance;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.play.NodeSnapshot;
import net.skykingdoms.backend.protocol.server.ServerScaleType;
import net.skykingdoms.backend.protocol.type.WrappedNodeSnapshot;

import java.util.Collection;
import java.util.Comparator;

/**
 * General nodes manager used to handle all shit with
 *
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class NodeManager {

    // ----------------------------------------------------------
    @Dependency
    public static final NodeManager INSTANCE = new NodeManager();
    // ----------------------------------------------------------

    /**
     * Set of registered node instance
     */
    Collection<NodeInstance> registeredNodes = CollectionUtil.createConcurrentWeakSet();

    /**
     * Checks whether the node registered or nah
     */
    public boolean isNodeRegistered(final @NonNull String nodeName) {
        return registeredNodes.stream()
                .anyMatch(s -> s.getName().equals(nodeName));
    }

    /**
     * Returns a node instance instance by its name
     */
    public NodeInstance getNodeServer(final @NonNull String nodeName) {
        return registeredNodes.stream()
                .filter(nodeServer -> nodeServer.getName().equals(nodeName))
                .findFirst()
                .orElse(null);
    }

    public NodeInstance getNodeServerFromAddress(final @NonNull String address) {
        return registeredNodes.stream()
                .filter(nodeServer -> nodeServer.getConnection().getHost().equalsIgnoreCase(address))
                .findFirst()
                .orElse(null);
    }

    //TODO: MAYBE REWRITE THIS NOT SURE, THE MAX RAM THING IS A FUCKERY
    /**
     * Get the most optimal node instance based on the given ServerScaleType.
     * This method filters the registered nodes to find those that match the specified scale type
     * and have allocated RAM less than or equal to their maximum RAM. It then returns the node
     * with the least allocated RAM.
     *
     * @param scaleType the ServerScaleType to match
     * @return the most optimal NodeServer that matches the given scale type and has the least allocated RAM,
     *         or null if no matching node is found
     */
    public NodeInstance getOptimalNodeServer(final @NonNull ServerScaleType scaleType) {
        return registeredNodes.stream()
                .filter(node -> {
                    for (ServerScaleType type : node.getScaleTypes()) {
                        if (type == scaleType && node.getAllocatedRamMB() <= node.getMaxRamMB()) {
                            return true;
                        }
                    }
                    return false;
                })
                .min(Comparator.comparingInt(NodeInstance::getAllocatedRamMB))
                .orElse(null);
    }

    /**
     * Basically, registers a node in the set
     */
    public NodeInstance registerNode(final @NonNull NodeInstance server) {
        if (isNodeRegistered(server.getName())) {
            throw new IllegalStateException("Node with this name is already registered!");
        }

        Logging.IMP.info("[Node] Registered node agent {}", server.getName());

        registeredNodes.add(server);

        return server;
    }

    /**
     * Removes a node from set of registered nodes
     */
    public void removeNode(final @NonNull NodeInstance server) {
        Logging.IMP.info("[Node] Unregistered node agent {}", server.getName());

        registeredNodes.remove(server);
    }


    public WrappedNodeSnapshot requestSnapshot(final @NonNull String nodeName) {
        if (!isNodeRegistered(nodeName)) {
            return null;
        }

        return getNodeServer(nodeName).getChannel()
                .awaitPacket(new NodeSnapshot.Request())
                .thenApply(NodeSnapshot.Response::getWrappedNodeSnapshot)
                .join();
    }

}