package net.skykingdoms.backend.protocol.server;

import org.jetbrains.annotations.NotNull;


public interface ILinkedServer {

    @NotNull IPlayableBukkitServer getParentServer();

}
