package com.peco2282.devcore.scheduler

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

/**
 * A [CoroutineDispatcher] that dispatches blocks to the Bukkit scheduler.
 *
 * This allows Kotlin Coroutines to be executed on the Bukkit main thread,
 * ensuring thread-safety when interacting with the Bukkit API.
 *
 * @property plugin the [Plugin] instance to use for scheduling
 */
class BukkitDispatcher(val plugin: Plugin) : CoroutineDispatcher() {
  /**
   * Dispatches the execution of a runnable block to the Bukkit main thread.
   *
   * @param context the coroutine context
   * @param block the runnable block to execute
   */
  override fun dispatch(context: CoroutineContext, block: Runnable) {
    plugin.scheduler.sync { block.run() }
  }
}
