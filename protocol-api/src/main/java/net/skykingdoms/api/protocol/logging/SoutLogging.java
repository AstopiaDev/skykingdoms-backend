package net.skykingdoms.api.protocol.logging;


import lombok.NonNull;
import net.skykingdoms.impl.Impl;
import net.skykingdoms.impl.ImplPriority;

import java.lang.management.ManagementFactory;


@Impl(value = Logging.class, priority = ImplPriority.HIGHEST)
public final class SoutLogging extends Logging {

    private final boolean debug;

    public SoutLogging() {
        boolean debug = false;

        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.contains("jdwp=")) {
                debug = true;
                break;
            }
        }

        this.debug = debug;
    }

    @Override
    public void debug(final @NonNull String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    @Override
    public void info(final @NonNull String message) {
        System.out.println(message);
    }

    @Override
    public void warn(final @NonNull String message) {
        System.out.println(message);
    }

    @Override
    public void error(final @NonNull String message) {
        System.err.println(message);
    }

    @Override
    public void stacktrace(final @NonNull Throwable t,
                           final @NonNull String message) {
        System.err.println(message);

        t.printStackTrace();
    }
}
