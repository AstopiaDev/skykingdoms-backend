package net.skykingdoms.backend.node.boot;

import net.skykingdoms.backend.node.NodeApp;
import net.skykingdoms.backend.protocol.logging.Logging;
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
