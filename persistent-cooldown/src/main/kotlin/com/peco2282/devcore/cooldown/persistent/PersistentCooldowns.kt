package com.peco2282.devcore.cooldown.persistent

import com.peco2282.devcore.cooldown.SystemTimeSource
import com.peco2282.devcore.cooldown.TimeSource
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class PersistentCooldowns(
  plugin: Plugin,
  private val timeSource: TimeSource = SystemTimeSource
) {
  private val prefix = plugin.name.lowercase().replace(Regex("[^a-z0-9._-]"), "_")

  private fun key(id: String): NamespacedKey {
    require(id.matches(Regex("[a-z0-9._/-]+"))) { "Cooldown id must be a lowercase namespaced path" }
    return NamespacedKey(prefix, "cooldown/$id")
  }

  fun remaining(player: Player, id: String): Duration {
    val key = key(id)
    val expiry = player.persistentDataContainer.get(key, PersistentDataType.LONG) ?: return Duration.ZERO
    val left = expiry - timeSource.nowMillis()
    if (left <= 0) { player.persistentDataContainer.remove(key); return Duration.ZERO }
    return left.milliseconds
  }

  fun isReady(player: Player, id: String): Boolean = remaining(player, id) == Duration.ZERO

  fun tryUse(player: Player, id: String, duration: Duration): Boolean {
    require(!duration.isNegative()) { "duration must not be negative" }
    if (!isReady(player, id)) return false
    player.persistentDataContainer.set(key(id), PersistentDataType.LONG, timeSource.nowMillis() + duration.inWholeMilliseconds)
    return true
  }

  fun clear(player: Player, id: String) = player.persistentDataContainer.remove(key(id))
}
