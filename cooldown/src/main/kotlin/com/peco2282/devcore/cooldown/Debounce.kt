package com.peco2282.devcore.cooldown

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Handles debouncing for keys of type [K].
 *
 * Debouncing ensures that an action can only be performed if a certain amount of time
 * has passed since the last successful action for that specific key.
 *
 * @param K the type of the key used to identify debounce states
 * @property timeSource the source of current time, defaults to [SystemTimeSource]
 */
class Debounce<K: Any>(
  private val timeSource: TimeSource = SystemTimeSource
) {
  private val nextAllowedAtMillis = ConcurrentHashMap<K, Long>()

  /**
   * Clears the debounce state for the specified [key].
   *
   * @param key the key whose debounce state should be cleared
   */
  fun clear(key: K) {
    nextAllowedAtMillis.remove(key)
  }

  /**
   * Clears all debounce states currently managed by this instance.
   */
  fun clearAll() {
    nextAllowedAtMillis.clear()
  }

  /**
   * Returns the remaining time until the specified [key] is allowed to be used again, in milliseconds.
   *
   * @param key the key to check
   * @return the remaining time in milliseconds, or 0 if it is already allowed or not found
   */
  fun remainingMillis(key: K): Long {
    val now = timeSource.nowMillis()
    val next = nextAllowedAtMillis[key] ?: return 0L
    return max(0L, next - now)
  }

  /**
   * Returns whether the specified [key] is currently allowed to be used.
   *
   * @param key the key to check
   * @return true if the action is allowed, false otherwise
   */
  fun isAllowed(key: K): Boolean = remainingMillis(key) <= 0L

  /**
   * Tries to allow an action for the specified [key] with a minimum interval in milliseconds.
   *
   * If the current time is greater than or equal to the next allowed time, the action is allowed
   * and the next allowed time is updated to (current time + [minIntervalMillis]).
   *
   * @param key the key to check and update
   * @param minIntervalMillis the minimum interval between allowed actions in milliseconds
   * @return true if allowed and the next allowed time was updated, false otherwise
   * @throws IllegalArgumentException if [minIntervalMillis] is negative
   */
  fun allowEvery(key: K, minIntervalMillis: Long): Boolean {
    require(minIntervalMillis >= 0) { "minIntervalMillis must be >= 0" }

    val now = timeSource.nowMillis()
    val next = nextAllowedAtMillis[key] ?: 0L
    if (now < next) return false

    nextAllowedAtMillis[key] = now + minIntervalMillis
    return true
  }

  /**
   * Tries to allow an action for the specified [key] with a minimum interval using a [Duration].
   *
   * @param key the key to check and update
   * @param minInterval the minimum interval [Duration] between allowed actions
   * @return true if allowed and the next allowed time was updated, false otherwise
   */
  fun allowEvery(key: K, minInterval: Duration): Boolean =
    allowEvery(key, minInterval.toLong(DurationUnit.MILLISECONDS))
}

