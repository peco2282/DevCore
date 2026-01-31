package com.peco2282.devcore.sheduler

import org.bukkit.entity.Player

val Always = Condition.ALWAYS
val Never = Condition.NEVER

class PlayerOnline(private val player: Player) : Condition {
  override fun canRun() = player.isOnline
}

class RunLimit(private val max: Int) : Condition {
  private var count = 0
  override fun canRun(): Boolean {
    count++
    return count <= max
  }
}

class TimeLimit(duration: Ticks) : Condition {
  private val end = System.currentTimeMillis() + duration.value * 50
  override fun canRun() = System.currentTimeMillis() < end
}


class ConditionalRepeating(
  private val scheduler: Scheduler,
  private val delay: Ticks,
  private val period: Ticks
): Runner {
  private var condition: Condition = Always

  fun whileCondition(cond: Condition) = apply {
    condition = condition and cond
  }

  fun limitRuns(times: Int) = apply {
    condition = condition and RunLimit(times)
  }

  fun limitTime(duration: Ticks) = apply {
    condition = condition and TimeLimit(duration)
  }

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

