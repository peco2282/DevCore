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


/**
 * Safely casts this object to the specified type [E], returning null if the cast fails.
 *
 * @param E The target type to cast to. Must be a subtype of [T].
 * @param orThrow If true, throws [IllegalArgumentException] when cast fails; if false, returns null.
 * @return The casted object of type [E], or null if cast fails and [orThrow] is false.
 * @throws IllegalArgumentException if [orThrow] is true and the cast fails.
 *
 * @sample
 * ```kotlin
 * val obj: Any = "Hello"
 * val str: String? = obj.asNullable<Any, String>() // Returns "Hello"
 * val num: Int? = obj.asNullable<Any, Int>() // Returns null
 * val forced: Int? = obj.asNullable<Any, Int>(orThrow = true) // Throws IllegalArgumentException
 * ```
 */
inline fun <T, reified E: T> T.asNullable(orThrow: Boolean = false): E? = (this as? E).also { if (orThrow && it == null) throw IllegalArgumentException("Cannot cast to non-null type ${E::class.qualifiedName}") }

/**
 * Casts this object to the specified type [E] without null safety checks.
 *
 * @param E The target type to cast to. Must be a subtype of [T].
 * @return The casted object of type [E].
 * @throws ClassCastException if the cast is invalid.
 *
 * @sample
 * ```kotlin
 * val obj: Any = "Hello"
 * val str: String = obj.asNotNullable<Any, String>() // Returns "Hello"
 * ```
 */
inline fun <T, reified E: T> T.asNotNullable(): E = this as E

/**
 * Exception thrown when a feature requires a newer Minecraft version than currently running.
 *
 * @param current The current Minecraft version string.
 * @param target The minimum required Minecraft version string.
 */
class UnsupportedVersionException(current: String, target: String) : RuntimeException("This feature is not supported in the $current version of Minecraft. Please update to the $target version or higher of Minecraft.")

/**
 * Checks if the current Minecraft server version meets the minimum required version.
 *
 * @param target The minimum required Minecraft version string (e.g., "1.20.6").
 * @throws UnsupportedVersionException if the current version is lower than the target version.
 *
 * @sample
 * ```kotlin
 * checkVersion("1.20.6") // Throws if server is running 1.20.5 or lower
 * ```
 */
@Throws(UnsupportedVersionException::class)
fun checkVersion(target: String) {
  val current = Bukkit.getServer().minecraftVersion

  if (!isVersionAtLeast(current, target)) {
    throw UnsupportedVersionException(current, target)
  }
}

/**
 * Compares two version strings to determine if the current version is at least the target version.
 *
 * Version strings are compared part by part (major.minor.patch). Missing parts default to 0.
 *
 * @param current The current version string.
 * @param target The target version string to compare against.
 * @return true if current version is greater than or equal to target version, false otherwise.
 */
private fun isVersionAtLeast(current: String, target: String): Boolean {
  val currentParts = current.split('.')
  val targetParts = target.split('.')

  for (i in targetParts.indices) {
    val currentPart = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
    val targetPart = targetParts[i].toInt()

    if (currentPart < targetPart) return false
  }

  return true
}

/**
 * Marks declarations that are internal to DevCore and should not be used by external consumers.
 *
 * APIs marked with this annotation may change or be removed without warning in minor or patch releases.
 */
@RequiresOptIn("This API is internal to DevCore and may change without a major version bump.")
annotation class DevCoreInternal

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
fun Plugin.launch(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend CoroutineScope.() -> Unit
): Job {
  return this.scope.launch(context, block = block)
}

/**
 * Functional interface for operations that may throw exceptions.
 *
 * Useful for wrapping code that might fail into a callable interface,
 * often used with [getOrDefault] for safe execution with fallback values.
 *
 * @param T The type of value returned by the getter.
 */
fun interface ThrowableGetter<T> {
  @Throws(Throwable::class)
  fun get(): T
}

/**
 * Executes a [ThrowableGetter] and returns its result, or a default value if it throws an exception.
 *
 * @param T The type of value to return.
 * @param default The default value to return if [getter] throws an exception.
 * @param getter The [ThrowableGetter] to execute.
 * @return The result of [getter.get()] if successful, otherwise [default].
 *
 * @sample
 * ```kotlin
 * val config = getOrDefault("default.yml") {
 *     File("config.yml").readText()
 * }
 * ```
 */
fun <T> getOrDefault(default: T, getter: ThrowableGetter<T>): T = try {
  getter.get()
} catch (e: Throwable) {
  default
}
