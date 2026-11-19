package com.peco2282.devcore.cache

import com.peco2282.devcore.cooldown.TimeSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class TtlCacheTest {
  private class Clock(var now: Long = 0) : TimeSource { override fun nowMillis() = now }

  @Test fun `entries expire lazily and loader reruns`() {
    val clock = Clock()
    val cache = ttlCache<String, String>(5.seconds, timeSource = clock)
    assertEquals("a", cache.getOrPut("x") { "a" })
    clock.now = 5_000
    assertNull(cache["x"])
    assertEquals("b", cache.getOrPut("x") { "b" })
  }

  @Test fun `least recently used entry is evicted`() {
    val cache = ttlCache<String, Int>(5.seconds, maxSize = 2)
    cache.put("a", 1); cache.put("b", 2); cache["a"]; cache.put("c", 3)
    assertNull(cache["b"])
    assertEquals(1, cache["a"])
  }
}
