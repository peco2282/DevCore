package com.peco2282.devcore.util.animation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnimationTest {
  @Test
  fun `easings preserve endpoints`() {
    Easing.entries.forEach { easing ->
      assertEquals(0.0, easing(0.0), 1e-9, easing.name)
      assertEquals(1.0, easing(1.0), 1e-9, easing.name)
    }
  }

  @Test
  fun `standard easings stay finite and bounded where documented`() {
    listOf(Easing.LINEAR, Easing.IN_SINE, Easing.OUT_SINE, Easing.IN_OUT_SINE,
      Easing.IN_CUBIC, Easing.OUT_CUBIC, Easing.IN_OUT_CUBIC).forEach { easing ->
      val values = (0..100).map { easing(it / 100.0) }
      assertTrue(values.zipWithNext().all { (a, b) -> b >= a })
      assertTrue(values.all { it in 0.0..1.0 })
    }
  }

  @Test
  fun `timeline samples inclusive endpoints`() {
    val tween = tween(10.0, 20.0, frames = 4, easing = Easing.LINEAR)
    assertEquals(listOf(10.0, 12.5, 15.0, 17.5, 20.0), tween.frames().toList())
    assertEquals(10.0, tween.valueAt(-1))
    assertEquals(20.0, tween.valueAt(99))
  }
}
