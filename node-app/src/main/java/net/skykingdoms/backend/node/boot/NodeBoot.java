package net.skykingdoms.backend.node.boot;

import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.node.NodeApp;
import org.jetbrains.annotations.NotNull;

public class NodeBoot {
    public static void main(final String @NotNull [] args) {
        Logging.IMP.info("[EntryPoint] Bootstrapping node driver ...");
        try {
            NodeApp.INSTANCE.init();
        } catch (Throwable throwable) {
            Logging.IMP.stacktrace(throwable, "Internal error occurred");
        }
    }
}
