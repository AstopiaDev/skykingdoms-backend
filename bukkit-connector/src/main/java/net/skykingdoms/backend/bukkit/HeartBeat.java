package net.skykingdoms.backend.bukkit;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@UtilityClass
public final class HeartBeat {

    public static final int TWENTY_MINUTES = 24000;
    public static final int TEN_MINUTES = 12000;
    public static final int FIVE_MINUTES = 6000;
    public static final int ONE_MINUTE = 1200;

    public static final BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    public static @NotNull BukkitTask runAsync(final @NonNull Runnable async) {
        return SCHEDULER.runTaskAsynchronously(BukkitConnector.INSTANCE.plugin(), async);
    }

    public static @NotNull BukkitTask runSync(final @NonNull Runnable sync) {
        return Bukkit.getScheduler().runTask(BukkitConnector.INSTANCE.plugin(), sync);
    }

    public static void callEvent(final @NonNull Event event) {
        callEvent(event, null);
    }

    public static <T extends Event> void callEvent(final @NonNull T event,
                                                   final @Nullable Consumer<T> callback) {
        Runnable callEvent = () -> {
            Bukkit.getPluginManager().callEvent(event);

            if (callback != null) {
                callback.accept(event);
            }
        };

        val primaryThread = Bukkit.isPrimaryThread();
        val async = event.isAsynchronous();

        if (async && primaryThread) {
            runAsync(callEvent);
        } else if (!async && !primaryThread) {
            runSync(callEvent);
        } else {
            callEvent.run();
        }
    }

    public static @NotNull BukkitTask runTimerSync(final @NonNull Runnable runnable,
                                                   final long initial,
                                                   final long delay) {
        return runTimerSync(BukkitConnector.INSTANCE.plugin(), runnable, initial, delay);
    }

    public static @NotNull BukkitTask runTimerSync(final @NonNull Plugin plugin,
                                                   final @NonNull Runnable runnable,
                                                   final long initial,
                                                   final long delay) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, initial, delay);
    }

    public static @NotNull BukkitTask runLaterSync(final @NonNull Runnable runnable,
                                          final long delay) {
        return runLaterSync(BukkitConnector.INSTANCE.plugin(), runnable, delay);
    }

    public static @NotNull BukkitTask runLaterSync(final @NonNull Plugin plugin,
                                          final @NonNull Runnable runnable,
                                          final long delay) {
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    public static @NotNull BukkitTask runLaterAsync(final @NonNull Runnable runnable,
                                                    final long delay) {
        return SCHEDULER.runTaskLaterAsynchronously(BukkitConnector.INSTANCE.plugin(), runnable, delay);
    }

    public static @NotNull BukkitTask runTimerAsync(final @NonNull Runnable runnable,
                                           final long initial,
                                           final long delay) {
        return SCHEDULER.runTaskTimerAsynchronously(BukkitConnector.INSTANCE.plugin(), runnable, initial, delay);
    }

}
