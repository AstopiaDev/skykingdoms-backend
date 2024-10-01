package net.skykingdoms.api.protocol.logging;

import net.skykingdoms.api.protocol.util.TextFormat;
import net.skykingdoms.impl.Impls;

public abstract class Logging {

    public static final Logging IMP = Impls.get(Logging.class);

    public abstract void debug(String message);

    public void debug(String message, Object... args) {
        debug(TextFormat.formatText(message, args));
    }

    public abstract void info(String message);

    public void info(String message, Object... args) {
        info(TextFormat.formatText(message, args));
    }

    public abstract void warn(String message);

    public void warn(String message, Object... args) {
        warn(TextFormat.formatText(message, args));
    }

    public abstract void error(String message);

    public void error(String message, Object... args) {
        error(TextFormat.formatText(message, args));
    }

    public abstract void stacktrace(Throwable t, String message);

    public void stacktrace(Throwable t, String message, Object... args) {
        stacktrace(t, TextFormat.formatText(message, args));
    }

}
