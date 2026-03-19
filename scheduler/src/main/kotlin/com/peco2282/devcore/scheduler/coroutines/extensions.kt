package com.peco2282.devcore.scheduler.coroutines

import com.peco2282.devcore.scheduler.Ticks
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 * Custom dispatcher that runs tasks on the Bukkit main thread.
 */
class BukkitDispatcher(private val plugin: Plugin) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) return
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            Bukkit.getScheduler().runTask(plugin, block)
        }
    }
}

private val exceptionHandlerCache = ConcurrentHashMap<Plugin, CoroutineExceptionHandler>()

/**
 * Gets or sets the [CoroutineExceptionHandler] for this [Plugin].
 */
var Plugin.coroutineExceptionHandler: CoroutineExceptionHandler
    get() = exceptionHandlerCache.getOrPut(this) {
        CoroutineExceptionHandler { _, throwable ->
            logger.severe("Coroutine exception in plugin ${this.name}:")
            throwable.printStackTrace()
        }
    }
    set(value) {
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
    get() = dispatcherCache.getOrPut(this) { BukkitDispatcher(this) }

/**
 * Gets a [CoroutineScope] bound to this [Plugin]'s lifecycle on the main thread.
 */
val Plugin.scope: CoroutineScope
    get() = CoroutineScope(dispatcher + SupervisorJob() + coroutineExceptionHandler)

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
