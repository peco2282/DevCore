package com.peco2282.devcore.cooldown

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.DurationUnit

class Cooldowns<K: Any>(
  private val timeSource: TimeSource = SystemTimeSource
) {
  private val expiresAtMillis = ConcurrentHashMap<K, Long>()

  fun clear(key: K) {
    expiresAtMillis.remove(key)
  }

  fun clearAll() {
    expiresAtMillis.clear()
  }

  fun remainingMillis(key: K): Long {
    val now = timeSource.nowMillis()
    val expiry = expiresAtMillis[key] ?: return 0L
    return max(0L, expiry - now)
  }

  fun isReady(key: K): Boolean = remainingMillis(key) <= 0L

  fun set(key: K, cooldownMillis: Long) {
    require(cooldownMillis >= 0) { "cooldownMillis must be >= 0" }
    expiresAtMillis[key] = timeSource.nowMillis() + cooldownMillis
  }

  fun set(key: K, cooldown: Duration) {
    set(key, cooldown.toLong(DurationUnit.MILLISECONDS))
  }

  fun tryUse(key: K, cooldownMillis: Long): Boolean {
    if (!isReady(key)) return false
    set(key, cooldownMillis)
    return true
  }

  fun tryUse(key: K, cooldown: Duration): Boolean = tryUse(key, cooldown.toLong(DurationUnit.MILLISECONDS))
}

