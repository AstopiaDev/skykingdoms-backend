package net.skykingdoms.backend.protocol.server;

import net.skykingdoms.backend.protocol.channel.AbstractRemoteClientChannel;
import org.jetbrains.annotations.NotNull;

/**
 * Represents any CLIENT that can be connected to Coordinator instance.
 * This could be a game instance server, proxy instance server, node connector, watchdog etc..
 *
 */
public interface IConnectable {

    /**
     * @return the name of remote. Received in handshake
     */
    @NotNull String getName();

    /**
     * @return address of remote
     */
    @NotNull AbstractRemoteClientChannel getChannel();

}
