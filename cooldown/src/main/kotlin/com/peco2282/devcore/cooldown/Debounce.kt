package com.peco2282.devcore.cooldown

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.DurationUnit

class Debounce<K: Any>(
  private val timeSource: TimeSource = SystemTimeSource
) {
  private val nextAllowedAtMillis = ConcurrentHashMap<K, Long>()

  fun clear(key: K) {
    nextAllowedAtMillis.remove(key)
  }

  fun clearAll() {
    nextAllowedAtMillis.clear()
  }

  fun remainingMillis(key: K): Long {
    val now = timeSource.nowMillis()
    val next = nextAllowedAtMillis[key] ?: return 0L
    return max(0L, next - now)
  }

  fun isAllowed(key: K): Boolean = remainingMillis(key) <= 0L

  fun allowEvery(key: K, minIntervalMillis: Long): Boolean {
    require(minIntervalMillis >= 0) { "minIntervalMillis must be >= 0" }

    val now = timeSource.nowMillis()
    val next = nextAllowedAtMillis[key] ?: 0L
    if (now < next) return false

    nextAllowedAtMillis[key] = now + minIntervalMillis
    return true
  }

  fun allowEvery(key: K, minInterval: Duration): Boolean =
    allowEvery(key, minInterval.toLong(DurationUnit.MILLISECONDS))
}

