package com.peco2282.devcore.cooldown

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

private class FakeTimeSource(var now: Long = 0L) : TimeSource {
  override fun nowMillis(): Long = now
}

class CooldownsTest {
  @Test
  fun tryUse_sets_expiry_and_blocks_until_elapsed() {
    val time = FakeTimeSource(0L)
    val cooldowns = Cooldowns<String>(time)

    assertTrue(cooldowns.tryUse("a", 100.milliseconds))
    assertFalse(cooldowns.tryUse("a", 100.milliseconds))
    assertEquals(100L, cooldowns.remainingMillis("a"))

    time.now = 99L
    assertFalse(cooldowns.isReady("a"))
    assertEquals(1L, cooldowns.remainingMillis("a"))

    time.now = 100L
    assertTrue(cooldowns.isReady("a"))
    assertEquals(0L, cooldowns.remainingMillis("a"))
    assertTrue(cooldowns.tryUse("a", 50.milliseconds))
  }
}

