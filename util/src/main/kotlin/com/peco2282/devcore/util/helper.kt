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

inline fun <T, reified E: T> T.asNullable(orThrow: Boolean = false): E? = (this as? E).also { if (orThrow && it == null) throw IllegalArgumentException("Cannot cast to non-null type ${E::class.qualifiedName}") }

inline fun <T, reified E: T> T.asNotNullable(): E = this as E

class UnsupportedVersionException(current: String, target: String) : RuntimeException("This feature is not supported in the $current version of Minecraft. Please update to the $target version or higher of Minecraft.")

@Throws(UnsupportedVersionException::class)
fun checkVersion(target: String) {
  val current = Bukkit.getServer().minecraftVersion

  if (!isVersionAtLeast(current, target)) {
    throw UnsupportedVersionException(current, target)
  }
}

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

@RequiresOptIn("This API is internal to DevCore and may change without a major version bump.")
annotation class DevCoreInternal

internal val coroutinesCatch = mutableMapOf<Plugin, CoroutineDispatcher>()

val Plugin.coroutine: CoroutineDispatcher get() = coroutinesCatch.getOrPut(this) { BukkitDispatcher(this) }

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

val Plugin.scope: CoroutineScope
  get() = scopeMap.getOrPut(this) {
    CoroutineScope(SupervisorJob() + coroutine)
  }


fun Plugin.launch(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend CoroutineScope.() -> Unit
): Job {
  return this.scope.launch(context, block = block)
}

fun interface ThrowableGetter<T> {
  @Throws(Throwable::class)
  fun get(): T
}

fun <T> getOrDefault(default: T, getter: ThrowableGetter<T>): T = try {
  getter.get()
} catch (e: Throwable) {
  default
}
