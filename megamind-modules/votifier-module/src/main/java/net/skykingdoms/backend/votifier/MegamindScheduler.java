package net.skykingdoms.backend.votifier;

import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.votifier.platform.scheduler.ScheduledVotifierTask;
import net.skykingdoms.backend.votifier.platform.scheduler.VotifierScheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MegamindScheduler implements VotifierScheduler {
    @Override
    public ScheduledVotifierTask delayedOnPool(Runnable runnable, int delay, TimeUnit unit) {
        return new MegamindTaskWrapper(MegamindApp.INSTANCE.scheduler().schedule(runnable, delay, unit));
    }

    @Override
    public ScheduledVotifierTask repeatOnPool(Runnable runnable, int delay, int repeat, TimeUnit unit) {
        return new MegamindTaskWrapper(MegamindApp.INSTANCE.scheduler().scheduleAtFixedRate(runnable, delay, repeat, unit));
    }

    private static class MegamindTaskWrapper implements ScheduledVotifierTask {
        private final ScheduledFuture<?> task;

        private MegamindTaskWrapper(ScheduledFuture<?> task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel(true);
        }
    }
}
