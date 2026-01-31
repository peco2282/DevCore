package com.peco2282.devcore.sheduler

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

class Scheduler(
  internal val plugin: Plugin
) {
  private val scheduler by lazy { Bukkit.getScheduler() }
  internal val manager by lazy { TaskManager(plugin) }

  private fun wrap(task: () -> Unit): Runnable = Runnable {
    try {
      task()
    } catch (e: Throwable) {
      plugin.logger.severe("Task crashed: ${e.message}")
      e.printStackTrace()
    }
  }

  private fun track(task: BukkitTask): TaskHandle {
    val handle = BukkitTaskHandle(plugin, task)
    plugin.taskManager.track(handle)
    return handle
  }

  fun later(delay: Ticks, task: () -> Unit) =
    track(scheduler.runTaskLater(plugin, wrap(task), delay.value))

  fun timer(delay: Ticks, period: Ticks, task: () -> Unit) =
    track(scheduler.runTaskTimer(plugin, wrap(task), delay.value, period.value))

  fun sync(task: () -> Unit) =
    track(scheduler.runTask(plugin, wrap(task)))

  fun async(task: () -> Unit) =
    track(scheduler.runTaskAsynchronously(plugin, wrap(task)))

  fun timerAsync(delay: Ticks, period: Ticks, task: () -> Unit): TaskHandle =
    track(scheduler.runTaskTimerAsynchronously(plugin, wrap(task), delay.value, period.value))
}

