package net.skykingdoms.backend.protocol.packet;

import org.jetbrains.annotations.NotNull;

public interface PacketProcessor {

    /**
     * Process error
     */
    default void process(final @NotNull Throwable throwable) throws Exception {
        throwable.printStackTrace();

        // for implementation
    }

    /**
     * Process packet
     */
    default void process(ChannelProcessorContext ctx) throws Exception {
        ctx.callProcess(this);
    }

    /**
     * Process connect
     */
    default void active() throws Exception {
        // for implementation
    }

    /**
     * Process disconnect
     */
    default void inactive() throws Exception {
        // for implementation
    }


}
