package net.skykingdoms.backend.base.util.concurrent;

import lombok.NonNull;

import java.util.concurrent.Executor;
public final class SimpleExecutor implements Executor {

    public static final SimpleExecutor INSTANCE = new SimpleExecutor();

    @Override
    public void execute(final @NonNull Runnable command) {
        command.run();
    }

}
