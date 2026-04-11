package com.peco2282.devcore.util

import org.bukkit.Bukkit

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