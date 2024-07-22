package net.skykingdoms.backend.bukkit.concurrent;

import lombok.NonNull;
import net.skykingdoms.backend.bukkit.HeartBeat;

import java.util.concurrent.Executor;

public final class BukkitSyncExecutor implements Executor {

    public static final BukkitSyncExecutor INSTANCE = new BukkitSyncExecutor();

    @Override
    public void execute(final @NonNull Runnable command) {
        HeartBeat.runSync(command);
    }
}
