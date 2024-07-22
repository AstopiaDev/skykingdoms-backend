package net.skykingdoms.backend.megamind;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public enum AsyncExecution {
    INSTANCE;

    public void run(final @NonNull Runnable task) {
        MegamindApp.INSTANCE.executor().submit(task);
    }

    public @NotNull ScheduledFuture<?> runLater(final @NonNull Runnable task,
                                                final long delay,
                                                final @NonNull TimeUnit unit) {
        return MegamindApp.INSTANCE.scheduler().schedule(task, delay, unit);
    }

    public @NotNull ScheduledFuture<?> runTimer(final @NonNull Runnable task,
                                                final long initial,
                                                final long delay,
                                                final @NonNull TimeUnit unit) {
        return MegamindApp.INSTANCE.scheduler().scheduleWithFixedDelay(task, initial, delay, unit);
    }
}
