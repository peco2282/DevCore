package com.peco2282.devcore.scheduler.coroutines

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.on
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 * Custom dispatcher that runs tasks on the Bukkit main thread.
 */
class BukkitDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
  @Volatile
  private var stopping = false

  internal fun stop() {
    stopping = true
  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    if (stopping || !plugin.isEnabled) {
      context[Job]?.cancel()
      block.run()
      return
    }
    if (Bukkit.isPrimaryThread()) {
      block.run()
    } else {
      Bukkit.getScheduler().runTask(plugin, block)
    }
  }
}

private val exceptionHandlerCache = ConcurrentHashMap<Plugin, CoroutineExceptionHandler>()
private val scopeCache = ConcurrentHashMap<Plugin, CoroutineScope>()
private val scopeListenerCache = ConcurrentHashMap<Plugin, Listener>()

/**
 * Gets or sets the [CoroutineExceptionHandler] for this [Plugin].
 */
var Plugin.coroutineExceptionHandler: CoroutineExceptionHandler
  get() {
    ensureScopeLifecycleListener()
    return exceptionHandlerCache.getOrPut(this) {
      CoroutineExceptionHandler { _, throwable ->
        logger.severe("Coroutine exception in plugin ${this.name}:")
        throwable.printStackTrace()
      }
    }
  }
  set(value) {
    ensureScopeLifecycleListener()
    exceptionHandlerCache[this] = value
  }

/**
 * Extension properties and functions for coroutine support in Bukkit.
 */
private val dispatcherCache = ConcurrentHashMap<Plugin, BukkitDispatcher>()

/**
 * Gets the [BukkitDispatcher] for this [Plugin].
 */
val Plugin.dispatcher: BukkitDispatcher
  get() {
    ensureScopeLifecycleListener()
    return dispatcherCache.getOrPut(this) { BukkitDispatcher(this) }
  }

/**
 * Gets a [CoroutineScope] bound to this [Plugin]'s lifecycle on the main thread.
 */
val Plugin.scope: CoroutineScope
  get() = scopeCache.computeIfAbsent(this) { plugin ->
    plugin.ensureScopeLifecycleListener()
    CoroutineScope(plugin.dispatcher + SupervisorJob() + plugin.coroutineExceptionHandler)
  }

private fun Plugin.ensureScopeLifecycleListener() {
  scopeListenerCache.computeIfAbsent(this) { plugin ->
    object : Listener {
      @EventHandler
      fun onPluginDisable(event: PluginDisableEvent) {
        if (event.plugin === plugin) plugin.cancelScope()
      }
    }.also { Bukkit.getPluginManager().registerEvents(it, plugin) }
  }
}

internal fun Plugin.cancelScope() {
  dispatcherCache.remove(this)?.stop()
  scopeCache.remove(this)?.cancel()
  scopeListenerCache.remove(this)?.let { HandlerList.unregisterAll(it) }
  exceptionHandlerCache.remove(this)
}

/**
 * Suspends the coroutine for the given number of [ticks].
 *
 * @param ticks the number of ticks to wait
 */
suspend fun delayTicks(ticks: Ticks) {
  if (ticks.value <= 0) return
  suspendCancellableCoroutine { continuation ->
    val plugin = Bukkit.getPluginManager().plugins.firstOrNull { it.isEnabled }
      ?: throw IllegalStateException("No enabled plugin found to schedule delay")

    val task = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
      continuation.resume(Unit)
    }, ticks.value)

    continuation.invokeOnCancellation {
      task.cancel()
    }
  }
}

/**
 * Suspends the coroutine for the given number of [ticks].
 *
 * This version allows specifying the [plugin] to use for scheduling.
 *
 * @param plugin the plugin to use for scheduling the delay
 * @param ticks the number of ticks to wait
 */
suspend fun delayTicks(plugin: Plugin, ticks: Ticks) {
  if (ticks.value <= 0) return
  suspendCancellableCoroutine { continuation ->
    val task = Bukkit.getScheduler().runTaskLater(plugin, Runnable {
      continuation.resume(Unit)
    }, ticks.value)

    continuation.invokeOnCancellation {
      task.cancel()
    }
  }
}
