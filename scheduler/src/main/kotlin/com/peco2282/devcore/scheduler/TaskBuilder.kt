package com.peco2282.devcore.scheduler

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * DSL for building and scheduling tasks.
 *
 * This class provides methods to define the execution timing and environment
 * (synchronous/asynchronous) of a task.
 *
 * @param plugin the [Plugin] instance to schedule tasks under
 */
class TaskBuilder(
  plugin: Plugin,
) {
  private val scheduler = plugin.scheduler

  /**
   * Schedules a task to run after the specified [delay].
   *
   * @param delay the amount of time to wait before running the task
   * @return a [DelayedTask] instance to continue the DSL
   */
  infix fun after(delay: Ticks) = DelayedTask(delay)

  /**
   * Runs the [task] on the next tick (synchronously on the main thread).
   *
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to cancel the task
   */
  infix fun now(task: () -> Unit) = scheduler.sync(task)

  /**
   * Runs the [task] asynchronously.
   *
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to cancel the task
   */
  infix fun async(task: () -> Unit) = scheduler.async(task)

  /**
   * Represents a task that is set to run after a certain delay.
   *
   * @property delay the delay before execution
   */
  inner class DelayedTask(
    private val delay: Ticks
  ) : Runner {
    /**
     * Schedules this task to repeat with the specified [period].
     *
     * @param period the interval between subsequent executions
     * @return a [RepeatingTask] instance to continue the DSL
     */
    infix fun every(period: Ticks) = RepeatingTask(delay, period)

    /**
     * Executes the [task] once after the [delay].
     *
     * @param task the block of code to execute
     * @return a [TaskHandle] that can be used to cancel the task
     */
    override infix fun run(task: () -> Unit): TaskHandle =
      scheduler.later(delay, task)
  }

  /**
   * Represents a task that is set to run repeatedly.
   *
   * @property delay the initial delay before the first execution
   * @property period the interval between subsequent executions
   */
  inner class RepeatingTask(
    private val delay: Ticks,
    private val period: Ticks
  ) : Runner {
    /**
     * Executes the [task] repeatedly according to the [delay] and [period].
     *
     * @param task the block of code to execute
     * @return a [TaskHandle] that can be used to cancel the task
     */
    override infix fun run(task: () -> Unit): TaskHandle =
      scheduler.timer(delay, period, task)
  }
}


fun main() {
  val plugin = object : JavaPlugin() {}
  val player = plugin.server.getPlayer("peco2282")!!
  val world = plugin.server.getWorld("world")!!
  // 通常
  plugin.taskCreate after 5.seconds run {
    println("実行")
  }

  player.taskAfter(plugin, 10.seconds) {
    player.sendMessage("まだログインしてたら表示")
  }

// ワールド依存ルーチン
  world.taskTimer(plugin, 0.ticks, 20.ticks) {
    println("ワールド存続中のみ実行")
  }

}
