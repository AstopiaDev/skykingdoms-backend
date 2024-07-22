package net.skykingdoms.backend.bukkit.concurrent;

import lombok.NonNull;
import net.skykingdoms.backend.bukkit.HeartBeat;

import java.util.concurrent.Executor;

public final class BukkitAsyncExecutor implements Executor {

    public static final BukkitAsyncExecutor INSTANCE = new BukkitAsyncExecutor();

    @Override
    public void execute(final @NonNull Runnable command) {
        HeartBeat.runAsync(command);
    }
}
