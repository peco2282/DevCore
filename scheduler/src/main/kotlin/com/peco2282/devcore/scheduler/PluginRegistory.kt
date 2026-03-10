package com.peco2282.devcore.scheduler

import org.bukkit.plugin.Plugin

/**
 * Registry for managing [Scheduler] instances per [Plugin].
 *
 * This singleton ensures that each plugin has exactly one [Scheduler] instance
 * and provides a centralized way to retrieve or remove them.
 */
object PluginRegistory {
  private val schedulerMap = mutableMapOf<Plugin, Scheduler>()

  /**
   * Gets or creates a [Scheduler] for the specified [plugin].
   *
   * @param plugin the [Plugin] instance
   * @return the [Scheduler] instance for the plugin
   */
  fun get(plugin: Plugin): Scheduler = schedulerMap.getOrPut(
    plugin
  ) { Scheduler(plugin) }

  /**
   * Removes the [Scheduler] for the specified [plugin] and cancels all its tasks.
   *
   * @param plugin the [Plugin] instance to remove the scheduler for
   * @return the removed [Scheduler] instance, or null if none existed
   */
  fun remove(plugin: Plugin) = schedulerMap.remove(plugin)?.manager?.cancelAll()
}
