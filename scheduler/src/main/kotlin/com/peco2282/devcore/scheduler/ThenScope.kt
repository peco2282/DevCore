package com.peco2282.devcore.scheduler

import org.bukkit.plugin.Plugin

/**
 * Scope for chaining tasks that should execute sequentially with a delay.
 *
 * @property plugin the [Plugin] instance to schedule tasks under
 * @property delay the amount of time to wait before executing the next task in the chain
 */
class ThenScope(
  private val plugin: Plugin,
  private val delay: Ticks
): Runner {
  /**
   * Executes the specified [task] after the [delay] associated with this scope.
   *
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to control the scheduled task
   */
  override infix fun run(task: () -> Unit): TaskHandle {
    return plugin.scheduler.later(delay, task)
  }
}

/**
 * Chains a task to be executed after the current task with the specified [delay].
 *
 * This allows for a fluent syntax to schedule sequential tasks:
 * `task1 then 20.ticks run { task2 }`
 *
 * @param delay the amount of time to wait after the previous task completes
 * @return a [ThenScope] instance to continue the chain
 */
infix fun TaskHandle.then(delay: Ticks): ThenScope {
  return ThenScope(scheduler().plugin, delay)
}


