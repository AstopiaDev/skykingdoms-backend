package net.skykingdoms.minimind;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public enum AsyncExecution {
    INSTANCE;

    private Minimind minimind;

    public void init(Minimind minimind) {
        this.minimind = minimind;
    }

    public void run(final @NonNull Runnable task) {
        minimind.getExecutor().submit(task);
    }

    public @NotNull ScheduledFuture<?> runLater(final @NonNull Runnable task,
                                                final long delay,
                                                final @NonNull TimeUnit unit) {
        return minimind.getScheduler().schedule(task, delay, unit);
    }

    public @NotNull ScheduledFuture<?> runTimer(final @NonNull Runnable task,
                                                final long initial,
                                                final long delay,
                                                final @NonNull TimeUnit unit) {
        return minimind.getScheduler().scheduleWithFixedDelay(task, initial, delay, unit);
    }
}
