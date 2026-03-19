package com.peco2282.devcore.scheduler

import kotlinx.coroutines.CoroutineScope
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import kotlin.coroutines.EmptyCoroutineContext

internal inline infix fun <reified E : Event> Plugin.on(crossinline action: E.() -> Unit) {
  Bukkit.getPluginManager().registerEvent(
    E::class.java,
    object : Listener {},
    EventPriority.NORMAL,
    { _, event ->
      if (event is E) action(event)
    },
    this,
    false
  )
}

/**
 * The [Scheduler] instance for this plugin.
 *
 * This provides access to the plugin-specific scheduler for task management.
 *
 * @return the [Scheduler] instance
 */
val Plugin.scheduler: Scheduler
  get() = PluginRegistory.get(this)

/**
 * The [TaskManager] instance for this plugin.
 *
 * This provides access to the task manager which tracks tasks for this plugin.
 *
 * @return the [TaskManager] instance
 */
val Plugin.taskManager: TaskManager
  get() = scheduler.manager

/**
 * Creates a new [TaskBuilder] for this plugin.
 *
 * This is the entry point for the task DSL.
 *
 * @return a new [TaskBuilder] instance
 */
val Plugin.taskCreate get() = TaskBuilder(this)

/**
 * Schedules a task to run after the specified [delay] and tracks it for this [Player].
 *
 * The task will be automatically cancelled when the player quits the server.
 *
 * @param plugin the [Plugin] instance to schedule the task under
 * @param delay the amount of time to wait before running the task
 * @param task the block of code to execute
 * @return a [TaskHandle] that can be used to cancel the task
 */
fun Player.taskAfter(plugin: Plugin, delay: Ticks, task: () -> Unit): TaskHandle {
  val handle = plugin.scheduler.later(delay, task)
  plugin.taskManager.trackPlayer(this, handle)
  return handle
}

/**
 * Schedules a repeated task with the specified [delay] and [period] and tracks it for this [World].
 *
 * The task will be automatically cancelled when the world is unloaded.
 *
 * @param plugin the [Plugin] instance to schedule the task under
 * @param delay the amount of time to wait before the first execution
 * @param period the interval between subsequent executions
 * @param task the block of code to execute
 * @return a [TaskHandle] that can be used to cancel the task
 */
fun World.taskTimer(plugin: Plugin, delay: Ticks, period: Ticks, task: () -> Unit): TaskHandle {
  val handle = plugin.scheduler.timer(delay, period, task)
  plugin.taskManager.trackWorld(this, handle)
  return handle
}

/**
 * Launches a coroutine on the Bukkit main thread and tracks it for this [Player].
 *
 * The coroutine will be automatically cancelled when the player quits the server.
 *
 * @param plugin the [Plugin] instance to schedule the task under
 * @param block the coroutine code
 * @return a [TaskHandle] that can be used to cancel the coroutine
 */
fun Player.taskLaunch(plugin: Plugin, block: suspend CoroutineScope.() -> Unit): TaskHandle {
  val handle = plugin.scheduler.launch(EmptyCoroutineContext, block)
  plugin.taskManager.trackPlayer(this, handle)
  return handle
}

/**
 * Launches a coroutine on the Bukkit main thread and tracks it for this [World].
 *
 * The coroutine will be automatically cancelled when the world is unloaded.
 *
 * @param plugin the [Plugin] instance to schedule the task under
 * @param block the coroutine code
 * @return a [TaskHandle] that can be used to cancel the coroutine
 */
fun World.taskLaunch(plugin: Plugin, block: suspend CoroutineScope.() -> Unit): TaskHandle {
  val handle = plugin.scheduler.launch(EmptyCoroutineContext, block)
  plugin.taskManager.trackWorld(this, handle)
  return handle
}

/**
 * Removes the scheduler for this plugin and cancels all associated tasks.
 *
 * This should typically be called when the plugin is being disabled.
 *
 * @return the [Scheduler] instance that was removed, or null if it didn't exist
 */
fun Plugin.removeScheduler() = PluginRegistory.remove(this)
