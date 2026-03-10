package com.peco2282.devcore.scheduler

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

/**
 * Represents a handle to a scheduled task.
 *
 * This interface provides methods to control and query the state of a task
 * that has been scheduled through the [Scheduler].
 */
interface TaskHandle {
  /**
   * Cancels the task.
   *
   * If the task is already running or has already been cancelled, this may have no effect.
   */
  fun cancel()

  /**
   * Returns whether the task has been cancelled.
   *
   * @return true if the task was cancelled, false otherwise
   */
  val isCancelled: Boolean

  /**
   * Returns the [Scheduler] instance that was used to create this task.
   *
   * @return the [Scheduler] instance
   */
  fun scheduler(): Scheduler
}

/**
 * A [TaskHandle] implementation for Bukkit tasks.
 *
 * @property plugin the [Plugin] that owns this task
 * @property task the underlying [BukkitTask] instance
 */
class BukkitTaskHandle(private val plugin: Plugin, private val task: BukkitTask) : TaskHandle {
  /**
   * Cancels the underlying Bukkit task.
   */
  override fun cancel() = task.cancel()

  /**
   * Checks if the underlying Bukkit task is cancelled.
   */
  override val isCancelled get() = task.isCancelled

  /**
   * Returns the scheduler for the associated plugin.
   */
  override fun scheduler() = plugin.scheduler
}
