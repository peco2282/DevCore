package com.peco2282.devcore.cooldown

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class DebounceTest {
  @Test
  fun allowEvery_allows_once_per_interval() {
    val time = object : TimeSource {
      var now = 0L
      override fun nowMillis(): Long = now
    }
    val debounce = Debounce<String>(time)

    assertTrue(debounce.allowEvery("a", 100.milliseconds))
    assertFalse(debounce.allowEvery("a", 100.milliseconds))
    assertEquals(100L, debounce.remainingMillis("a"))

    time.now = 50L
    assertFalse(debounce.allowEvery("a", 100.milliseconds))
    assertEquals(50L, debounce.remainingMillis("a"))

    time.now = 100L
    assertTrue(debounce.allowEvery("a", 100.milliseconds))
  }
}

