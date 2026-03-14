package com.peco2282.devcore.scheduler

import org.bukkit.entity.Player

/**
 * A condition that is always met.
 */
val Always = Condition.ALWAYS

/**
 * A condition that is never met.
 */
val Never = Condition.NEVER

/**
 * A condition that is met if the [player] is online.
 *
 * @property player the [Player] whose online status to check
 */
class PlayerOnline(private val player: Player) : Condition {
  /**
   * Checks if the player is currently online.
   *
   * @return true if the player is online, false otherwise
   */
  override fun canRun() = player.isOnline
}

/**
 * A condition that is met until the number of executions reaches [max].
 *
 * @property max the maximum number of times the task can run
 */
class RunLimit(private val max: Int) : Condition {
  private var count = 0

  /**
   * Increments the execution count and checks if it's within the limit.
   *
   * @return true if the execution count is less than or equal to [max], false otherwise
   */
  override fun canRun(): Boolean {
    count++
    return count <= max
  }
}

/**
 * A condition that is met until the specified [duration] has passed.
 *
 * @param duration the maximum duration to allow execution
 */
class TimeLimit(duration: Ticks) : Condition {
  private val end = System.currentTimeMillis() + duration.value * 50

  /**
   * Checks if the current time is before the calculated end time.
   *
   * @return true if within the time limit, false otherwise
   */
  override fun canRun() = System.currentTimeMillis() < end
}

/**
 * A [Runner] that executes a task repeatedly as long as a condition is met.
 *
 * @property scheduler the [Scheduler] instance to use
 * @property delay the initial delay before the first execution
 * @property period the interval between subsequent executions
 */
class ConditionalRepeating(
  private val scheduler: Scheduler,
  private val delay: Ticks = ZERO,
  private val period: Ticks = ZERO,
) : Runner {
  private var condition: Condition = Always

  /**
   * Adds a condition to be met for the task to continue running.
   *
   * Multiple conditions are combined using logical AND.
   *
   * @param cond the [Condition] to add
   * @return this [ConditionalRepeating] instance for chaining
   */
  fun whileCondition(cond: Condition) = apply {
    condition = condition and cond
  }

  /**
   * Limits the number of times the task can run.
   *
   * @param times the maximum number of runs
   * @return this [ConditionalRepeating] instance for chaining
   */
  fun limitRuns(times: Int) = apply {
    condition = condition and RunLimit(times)
  }

  /**
   * Limits the duration for which the task can run.
   *
   * @param duration the maximum duration
   * @return this [ConditionalRepeating] instance for chaining
   */
  fun limitTime(duration: Ticks) = apply {
    condition = condition and TimeLimit(duration)
  }

  /**
   * Starts the conditional repeating task.
   *
   * The task will run every [period] after an initial [delay] as long as [Condition.canRun]
   * returns true. Once the condition fails, the task is automatically cancelled.
   *
   * @param task the block of code to execute
   * @return a [TaskHandle] that can be used to manually cancel the task
   */
  override infix fun run(task: () -> Unit): TaskHandle {
    lateinit var handle: TaskHandle

    handle = scheduler.timer(delay, period) {
      if (!condition.canRun()) {
        handle.cancel()
        return@timer
      }
      task()
    }
    return handle
  }
}

