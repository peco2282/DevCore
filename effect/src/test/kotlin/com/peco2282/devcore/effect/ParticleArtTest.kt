package com.peco2282.devcore.effect

import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParticleArtTest {
  @Test fun `transparent pixels are skipped and point count is limited`() {
    val image = BufferedImage(4, 2, BufferedImage.TYPE_INT_ARGB)
    image.setRGB(0, 0, -1)
    image.setRGB(1, 0, -1)
    image.setRGB(2, 0, -1)
    assertEquals(2, imageShape(image, width = 4, maxPoints = 2).points().size)
  }

  @Test fun `text rendering works in headless mode`() {
    System.setProperty("java.awt.headless", "true")
    val points = textShape("DevCore", sampleStep = 4, maxPoints = 100).points()
    assertTrue(points.isNotEmpty())
    assertTrue(points.size <= 100)
  }
}
