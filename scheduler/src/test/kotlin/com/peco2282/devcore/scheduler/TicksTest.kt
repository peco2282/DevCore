package com.peco2282.devcore.scheduler

import kotlin.test.Test
import kotlin.test.assertEquals

class TicksTest {
  @Test
  fun `converts integer and long values to ticks`() {
    assertEquals(Ticks(0), ZERO)
    assertEquals(Ticks(12), 12.ticks)
    assertEquals(Ticks(42), 42L.ticks)
  }

  @Test
  fun `converts seconds to ticks`() {
    assertEquals(Ticks(40), 2.seconds)
    assertEquals(Ticks(30), 1.5.seconds)
    assertEquals(Ticks(15), 0.75f.seconds)
  }

  @Test
  fun `fractional seconds are truncated to whole ticks`() {
    assertEquals(Ticks(1), 0.099.seconds)
    assertEquals(Ticks(-1), (-0.099).seconds)
  }

  @Test
  fun `converts minutes to ticks`() {
    assertEquals(Ticks(1_200), 1.minutes)
    assertEquals(Ticks(3_600), 3.minutes)
  }
}
