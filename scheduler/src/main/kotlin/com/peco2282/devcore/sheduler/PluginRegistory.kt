package com.peco2282.devcore.sheduler

import org.bukkit.plugin.Plugin

object PluginRegistory {
  private val schedulerMap = mutableMapOf<Plugin, Scheduler>()

  fun get(plugin: Plugin): Scheduler = schedulerMap.getOrPut(
    plugin
  ) { Scheduler(plugin) }

  fun remove(plugin: Plugin) = schedulerMap.remove(plugin)?.manager?.cancelAll()
}
