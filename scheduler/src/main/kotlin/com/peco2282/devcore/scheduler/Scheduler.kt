package com.peco2282.devcore.scheduler

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

/**
 * Wrapper for the Bukkit scheduler.
 *
 * This class provides a simplified API for scheduling tasks on the Bukkit main thread
 * or asynchronously. It also automatically tracks tasks for lifecycle management.
 *
 * @property plugin the [Plugin] instance associated with this scheduler
 */
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

  /**
   * Runs the [task] after the specified [delay].
   *
   * @param delay the amount of time to wait before running the task
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to cancel the task
   */
  fun later(delay: Ticks, task: () -> Unit) =
    track(scheduler.runTaskLater(plugin, wrap(task), delay.value))

  /**
   * Runs the [task] repeatedly with the specified [delay] and [period].
   *
   * @param delay the amount of time to wait before the first execution
   * @param period the interval between subsequent executions
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to cancel the task
   */
  fun timer(delay: Ticks, period: Ticks, task: () -> Unit) =
    track(scheduler.runTaskTimer(plugin, wrap(task), delay.value, period.value))

  /**
   * Runs the [task] on the next tick (synchronously on the main thread).
   *
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to cancel the task
   */
  fun sync(task: () -> Unit) =
    track(scheduler.runTask(plugin, wrap(task)))

  /**
   * Runs the [task] asynchronously.
   *
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to cancel the task
   */
  fun async(task: () -> Unit) =
    track(scheduler.runTaskAsynchronously(plugin, wrap(task)))

  /**
   * Runs the [task] repeatedly and asynchronously with the specified [delay] and [period].
   *
   * @param delay the amount of time to wait before the first execution
   * @param period the interval between subsequent executions
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to cancel the task
   */
  fun timerAsync(delay: Ticks, period: Ticks, task: () -> Unit): TaskHandle =
    track(scheduler.runTaskTimerAsynchronously(plugin, wrap(task), delay.value, period.value))
}

