@file:JvmMultifileClass
@file:JvmName("HelperKt")

package com.peco2282.devcore.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("DEPRECATION")
internal val coroutinesCatch = mutableMapOf<Plugin, CoroutineDispatcher>()

/**
 * Gets or creates a [CoroutineDispatcher] for this plugin that dispatches work on the Bukkit main thread.
 *
 * The dispatcher ensures that coroutines run on the primary server thread if already on it,
 * or schedules them via [Bukkit.getScheduler] if called from another thread.
 *
 * @return A [CoroutineDispatcher] instance specific to this plugin.
 *
 * @sample
 * ```kotlin
 * class MyPlugin : JavaPlugin() {
 *     fun example() {
 *         val dispatcher = this.coroutine
 *         // Use in coroutine context
 *     }
 * }
 * ```
 */
@Deprecated("Use `com.peco2282.devcore.scheduler.coroutines` from the `scheduler` module. This copy is not cancelled on plugin disable.")
val Plugin.coroutine: CoroutineDispatcher get() = coroutinesCatch.getOrPut(this) { BukkitDispatcher(this) }

/**
 * Internal coroutine dispatcher that executes coroutines on the Bukkit main thread.
 *
 * This dispatcher checks if the current thread is the primary server thread:
 * - If yes, executes the block immediately
 * - If no, schedules execution via Bukkit's task scheduler
 *
 * @param plugin The plugin instance used for scheduling tasks.
 */
internal class BukkitDispatcher(val plugin: Plugin): CoroutineDispatcher() {
  override fun dispatch(context: CoroutineContext, block: Runnable) {
    if (Bukkit.isPrimaryThread()) {
      block.run()
    } else {
      Bukkit.getScheduler().runTask(plugin, block)
    }
  }
}

internal val scopeMap = mutableMapOf<Plugin, CoroutineScope>()

/**
 * Gets or creates a [CoroutineScope] for this plugin with a [SupervisorJob] and Bukkit dispatcher.
 *
 * The scope is tied to the plugin's lifecycle and uses [SupervisorJob] to prevent child coroutine
 * failures from cancelling the entire scope. All coroutines run on the Bukkit main thread.
 *
 * @return A [CoroutineScope] instance specific to this plugin.
 *
 * @sample
 * ```kotlin
 * class MyPlugin : JavaPlugin() {
 *     override fun onEnable() {
 *         scope.launch {
 *             // Coroutine code here runs on main thread
 *         }
 *     }
 * }
 * ```
 */
@Deprecated("Use `com.peco2282.devcore.scheduler.coroutines` from the `scheduler` module. This copy is not cancelled on plugin disable.")
val Plugin.scope: CoroutineScope
  get() = scopeMap.getOrPut(this) {
    CoroutineScope(SupervisorJob() + coroutine)
  }


/**
 * Launches a new coroutine within this plugin's scope on the Bukkit main thread.
 *
 * This is a convenience extension that uses the plugin's [scope] to launch coroutines.
 * The coroutine runs with the provided [context] merged with the plugin's dispatcher.
 *
 * @param context Additional coroutine context elements (default: [EmptyCoroutineContext]).
 * @param block The suspending lambda to execute within the coroutine.
 * @return A [Job] instance representing the launched coroutine.
 *
 * @sample
 * ```kotlin
 * class MyPlugin : JavaPlugin() {
 *     fun delayedTask() {
 *         launch {
 *             delay(1000)
 *             logger.info("Executed after 1 second")
 *         }
 *     }
 * }
 * ```
 */
@Deprecated("Use `com.peco2282.devcore.scheduler.coroutines` from the `scheduler` module. This copy is not cancelled on plugin disable.")
fun Plugin.launch(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend CoroutineScope.() -> Unit
): Job {
  return this.scope.launch(context, block = block)
}
