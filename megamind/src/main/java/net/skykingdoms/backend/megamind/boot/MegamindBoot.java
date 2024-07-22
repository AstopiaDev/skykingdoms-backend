package net.skykingdoms.backend.megamind.boot;

import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.protocol.logging.Logging;

public class MegamindBoot {
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("io.netty.selectorAutoRebuildThreshold", "0");

        try {
            MegamindApp.INSTANCE.init();
        } catch (Throwable t) {
            Logging.IMP.stacktrace(t, "Internal error occurred");
            System.exit(-1);
        }
    }
}
