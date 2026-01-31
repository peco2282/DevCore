package com.peco2282.devcore.cooldown

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Manages cooldowns for keys of type [K].
 *
 * This class provides a way to track and enforce time-based intervals between actions
 * associated with specific keys. It is thread-safe as it uses [ConcurrentHashMap] internally.
 *
 * @param K the type of the key used to identify cooldowns
 * @property timeSource the source of current time, defaults to [SystemTimeSource]
 */
class Cooldowns<K: Any>(
  private val timeSource: TimeSource = SystemTimeSource
) {
  private val expiresAtMillis = ConcurrentHashMap<K, Long>()

  /**
   * Clears the cooldown for the specified [key].
   *
   * After calling this, [isReady] for the given key will return true.
   *
   * @param key the key whose cooldown should be cleared
   */
  fun clear(key: K) {
    expiresAtMillis.remove(key)
  }

  /**
   * Clears all cooldowns currently managed by this instance.
   */
  fun clearAll() {
    expiresAtMillis.clear()
  }

  /**
   * Returns the remaining cooldown time in milliseconds for the specified [key].
   *
   * @param key the key to check the remaining cooldown for
   * @return the remaining time in milliseconds, or 0 if the cooldown has expired or the key is not found
   */
  fun remainingMillis(key: K): Long {
    val now = timeSource.nowMillis()
    val expiry = expiresAtMillis[key] ?: return 0L
    return max(0L, expiry - now)
  }

  /**
   * Returns whether the specified [key] is ready to be used.
   *
   * A key is ready if its cooldown has expired or if no cooldown was ever set for it.
   *
   * @param key the key to check
   * @return true if the key is ready, false otherwise
   */
  fun isReady(key: K): Boolean = remainingMillis(key) <= 0L

  /**
   * Sets the cooldown for the specified [key] in milliseconds.
   *
   * @param key the key to set the cooldown for
   * @param cooldownMillis the duration of the cooldown in milliseconds. Must be non-negative.
   * @throws IllegalArgumentException if [cooldownMillis] is negative
   */
  fun set(key: K, cooldownMillis: Long) {
    require(cooldownMillis >= 0) { "cooldownMillis must be >= 0" }
    expiresAtMillis[key] = timeSource.nowMillis() + cooldownMillis
  }

  /**
   * Sets the cooldown for the specified [key] using a [Duration].
   *
   * @param key the key to set the cooldown for
   * @param cooldown the [Duration] of the cooldown
   */
  fun set(key: K, cooldown: Duration) {
    set(key, cooldown.toLong(DurationUnit.MILLISECONDS))
  }

  /**
   * Tries to use the specified [key] with the given [cooldownMillis].
   *
   * If the key is ready, sets the cooldown and returns true. Otherwise, returns false.
   *
   * @param key the key to try to use
   * @param cooldownMillis the duration of the cooldown in milliseconds to set if ready
   * @return true if the key was ready and the cooldown was set, false otherwise
   */
  fun tryUse(key: K, cooldownMillis: Long): Boolean {
    if (!isReady(key)) return false
    set(key, cooldownMillis)
    return true
  }

  /**
   * Tries to use the specified [key] with the given [cooldown].
   *
   * If the key is ready, sets the cooldown and returns true. Otherwise, returns false.
   *
   * @param key the key to try to use
   * @param cooldown the [Duration] of the cooldown to set if ready
   * @return true if the key was ready and the cooldown was set, false otherwise
   */
  fun tryUse(key: K, cooldown: Duration): Boolean = tryUse(key, cooldown.toLong(DurationUnit.MILLISECONDS))
}

