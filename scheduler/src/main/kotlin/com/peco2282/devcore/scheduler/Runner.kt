package com.peco2282.devcore.scheduler

/**
 * Interface for executing tasks.
 *
 * This interface defines the final action in a task-building chain.
 */
interface Runner {
  /**
   * Runs the specified [task] according to the configuration of this runner.
   *
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to control or query the scheduled task
   */
  infix fun run(task: () -> Unit): TaskHandle
}
