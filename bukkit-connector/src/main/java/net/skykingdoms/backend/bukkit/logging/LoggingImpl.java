package net.skykingdoms.backend.bukkit.logging;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.impl.Impl;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Impl(Logging.class)
public final class LoggingImpl extends Logging {

    Logger logger;
    boolean debug;

    public LoggingImpl() {
        this.logger = Bukkit.getLogger();
        this.debug = Boolean.getBoolean("core.debug");
    }

    @Override
    public void debug(final @NonNull String message) {
        if (debug) {
            info(message);
        }
    }

    @Override
    public void info(final @NonNull String message) {
        this.logger.log(Level.INFO, message);
    }

    @Override
    public void warn(final @NonNull String message) {
        this.logger.log(Level.WARNING, message);
    }

    @Override
    public void error(final @NonNull String message) {
        this.logger.log(Level.SEVERE, message);
    }

    @Override
    public void stacktrace(final @NonNull Throwable t, final @NonNull String message) {
        this.logger.log(Level.SEVERE, message, t);
    }
}
