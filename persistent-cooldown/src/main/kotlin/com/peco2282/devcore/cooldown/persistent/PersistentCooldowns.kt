package com.peco2282.devcore.cooldown.persistent

import com.peco2282.devcore.cooldown.SystemTimeSource
import com.peco2282.devcore.cooldown.TimeSource
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Restart-resistant player cooldowns stored in the player's Persistent Data Container.
 *
 * Each ID maps to an epoch-millisecond deadline under the owning plugin's namespace. Expired
 * values are removed during [remaining]. IDs must be lowercase namespaced paths such as
 * `daily-reward` or `rewards/daily`. Because deadlines use wall-clock time, use this for long-lived
 * rewards rather than frame-precise combat actions.
 *
 * ```kotlin
 * val cooldowns = PersistentCooldowns(plugin)
 * if (cooldowns.tryUse(player, "daily-reward", 1.days)) {
 *   giveDailyReward(player)
 * } else {
 *   player.sendMessage("Remaining: ${cooldowns.remaining(player, "daily-reward")}")
 * }
 * cooldowns.clear(player, "daily-reward")
 * ```
 *
 * The operations must be called where accessing a player's PDC is safe (normally the server
 * thread). [timeSource] exists for deterministic tests.
 */
class PersistentCooldowns(
  plugin: Plugin,
  private val timeSource: TimeSource = SystemTimeSource
) {
  private val prefix = plugin.name.lowercase().replace(Regex("[^a-z0-9._-]"), "_")

  private fun key(id: String): NamespacedKey {
    require(id.matches(Regex("[a-z0-9._/-]+"))) { "Cooldown id must be a lowercase namespaced path" }
    return NamespacedKey(prefix, "cooldown/$id")
  }

  /** Returns time left, or zero when absent/expired; expired stored values are removed. */
  fun remaining(player: Player, id: String): Duration {
    val key = key(id)
    val expiry = player.persistentDataContainer.get(key, PersistentDataType.LONG) ?: return Duration.ZERO
    val left = expiry - timeSource.nowMillis()
    if (left <= 0) { player.persistentDataContainer.remove(key); return Duration.ZERO }
    return left.milliseconds
  }

  /** Whether [tryUse] would currently accept this player and ID. */
  fun isReady(player: Player, id: String): Boolean = remaining(player, id) == Duration.ZERO

  /** Atomically for normal single-threaded Bukkit use, starts [duration] when ready. */
  fun tryUse(player: Player, id: String, duration: Duration): Boolean {
    require(!duration.isNegative()) { "duration must not be negative" }
    if (!isReady(player, id)) return false
    player.persistentDataContainer.set(key(id), PersistentDataType.LONG, timeSource.nowMillis() + duration.inWholeMilliseconds)
    return true
  }

  /** Removes the stored deadline so the action becomes immediately ready. */
  fun clear(player: Player, id: String) = player.persistentDataContainer.remove(key(id))
}
