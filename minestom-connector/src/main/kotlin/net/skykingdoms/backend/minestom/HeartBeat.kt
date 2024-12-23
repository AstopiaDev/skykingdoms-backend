package net.skykingdoms.backend.minestom

import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.timer.SchedulerManager
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import java.util.function.Consumer

object HeartBeat {
    private val scheduler: SchedulerManager = MinecraftServer.getSchedulerManager()
    private val globalEventHandler: GlobalEventHandler = MinecraftServer.getGlobalEventHandler()

    const val TWENTY_MINUTES = 24000
    const val TEN_MINUTES = 12000
    const val FIVE_MINUTES = 6000
    const val ONE_MINUTE = 1200

    fun runAsync(runnable: Runnable): Task {
        return scheduler.submitTask {
            runnable.run()
            TaskSchedule.stop()
        }
    }

    fun runSync(runnable: Runnable): Task {
        return scheduler.scheduleNextTick(runnable)
    }

/*    fun runLaterAsync(runnable: Runnable, delay: Int): Task {
        return scheduler.scheduleTask(
            { runnable.run() },
            TaskSchedule.tick(delay)
        )
    }*/

    fun runTimerAsync(runnable: Runnable, initial: Int, delay: Int): Task {
        return scheduler.scheduleTask(
            { runnable.run() },
            TaskSchedule.tick(initial),
            TaskSchedule.tick(delay)
        )
    }

    fun callEvent(event: Event) {
        globalEventHandler.call(event)
    }

    fun <T : Event> callEvent(event: T, callback: Consumer<T>?) {
        val runnable = Runnable {
            globalEventHandler.call(event)
            callback?.accept(event)
        }

        if (Thread.currentThread().name.contains("Server thread")) {
            runSync(runnable)
        } else {
            runAsync(runnable)
        }
    }

/*    fun runLaterSync(runnable: Runnable, delay: Int): Task {
        return scheduler.scheduleTask(
            { runnable.run() },
            TaskSchedule.tick(delay)
        )
    }*/

    fun runTimerSync(runnable: Runnable, initial: Int, delay: Int): Task {
        return scheduler.scheduleTask(
            { runnable.run() },
            TaskSchedule.tick(initial),
            TaskSchedule.tick(delay)
        )
    }
}
