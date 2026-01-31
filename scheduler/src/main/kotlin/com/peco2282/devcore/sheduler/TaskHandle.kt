package com.peco2282.devcore.sheduler

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

interface TaskHandle {
  fun cancel()
  val isCancelled: Boolean

  fun scheduler(): Scheduler
}

class BukkitTaskHandle(private val plugin: Plugin, private val task: BukkitTask) : TaskHandle {
  override fun cancel() = task.cancel()
  override val isCancelled get() = task.isCancelled

  override fun scheduler() = plugin.scheduler
}
