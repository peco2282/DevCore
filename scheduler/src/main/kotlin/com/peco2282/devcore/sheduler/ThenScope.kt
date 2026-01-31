package com.peco2282.devcore.sheduler

import org.bukkit.plugin.Plugin

class ThenScope(
  private val plugin: Plugin,
  private val delay: Ticks
): Runner {
  override infix fun run(task: () -> Unit): TaskHandle {
    return plugin.scheduler.later(delay, task)
  }
}

infix fun TaskHandle.then(delay: Ticks): ThenScope {
  return ThenScope(scheduler().plugin, delay)
}


