package com.peco2282.devcore.cache

import com.peco2282.devcore.cooldown.SystemTimeSource
import com.peco2282.devcore.cooldown.TimeSource
import kotlin.time.Duration

/**
 * A small thread-safe cache whose entries expire after the configured TTL from insertion.
 *
 * Expiry and maximum-size eviction happen lazily during cache operations. [getOrPut] does not hold
 * the cache lock while computing, so concurrent misses may invoke the producer more than once; use
 * an idempotent producer. Values are never persisted.
 *
 * ```kotlin
 * val profiles = TtlCache<UUID, Profile>(5.minutes, maxSize = 500)
 * val profile = profiles.getOrPut(id) { repository.load(id) }
 * profiles.invalidate(id)
 * ```
 */
class TtlCache<K : Any, V : Any>(
  private val expireAfter: Duration,
  private val maxSize: Int,
  private val timeSource: TimeSource = SystemTimeSource
) {
  private data class Entry<V>(val value: V, val expiresAt: Long)
  private val entries = LinkedHashMap<K, Entry<V>>(16, 0.75f, true)

  init {
    require(expireAfter.isPositive()) { "expireAfter must be positive" }
    require(maxSize > 0) { "maxSize must be positive" }
  }

  @Synchronized operator fun get(key: K): V? {
    val entry = entries[key] ?: return null
    if (entry.expiresAt <= timeSource.nowMillis()) {
      entries.remove(key)
      return null
    }
    return entry.value
  }

  @Synchronized fun put(key: K, value: V): V {
    purgeExpired()
    entries[key] = Entry(value, timeSource.nowMillis() + expireAfter.inWholeMilliseconds)
    while (entries.size > maxSize) entries.remove(entries.entries.first().key)
    return value
  }

  fun getOrPut(key: K, create: () -> V): V = get(key) ?: put(key, create())
  @Synchronized fun invalidate(key: K): V? = entries.remove(key)?.value
  @Synchronized fun clear() = entries.clear()
  @Synchronized fun size(): Int { purgeExpired(); return entries.size }

  @Synchronized fun purgeExpired(): Int {
    val now = timeSource.nowMillis()
    val before = entries.size
    entries.entries.removeIf { it.value.expiresAt <= now }
    return before - entries.size
  }
}

/** Creates a [TtlCache] using monotonic time by default, which is safe from wall-clock changes. */
fun <K : Any, V : Any> ttlCache(
  expireAfter: Duration,
  maxSize: Int = Int.MAX_VALUE,
  timeSource: TimeSource = SystemTimeSource
): TtlCache<K, V> = TtlCache(expireAfter, maxSize, timeSource)
