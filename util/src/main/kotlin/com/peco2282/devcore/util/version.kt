@file:JvmMultifileClass
@file:JvmName("HelperKt")

package com.peco2282.devcore.util

import org.bukkit.Bukkit

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
