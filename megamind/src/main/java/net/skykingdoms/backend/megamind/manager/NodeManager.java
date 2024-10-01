package net.skykingdoms.backend.megamind.manager;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.dependency.Dependency;
import net.skykingdoms.backend.megamind.server.impl.NodeServer;
import net.skykingdoms.backend.protocol.packet.play.NodeSnapshot;
import net.skykingdoms.backend.protocol.type.WrappedNodeSnapshot;

import java.util.Collection;

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
     * Set of registered node server
     */
    Collection<NodeServer> registeredNodes = CollectionUtil.createConcurrentWeakSet();

    /**
     * Checks whether the node registered or nah
     */
    public boolean isNodeRegistered(final @NonNull String nodeName) {
        return registeredNodes.stream()
                .anyMatch(s -> s.getName().equals(nodeName));
    }

    /**
     * Returns a node server instance by its name
     */
    public NodeServer getNodeServer(final @NonNull String nodeName) {
        return registeredNodes.stream()
                .filter(nodeServer -> nodeServer.getName().equals(nodeName))
                .findFirst()
                .orElse(null);
    }

    public NodeServer getNodeServerFromAddress(final @NonNull String address) {
        return registeredNodes.stream()
                .filter(nodeServer -> nodeServer.getConnection().getHost().equalsIgnoreCase(address))
                .findFirst()
                .orElse(null);
    }

    /**
     * Basically, registers a node in the set
     */
    public NodeServer registerNode(final @NonNull NodeServer server) {
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
    public void removeNode(final @NonNull NodeServer server) {
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