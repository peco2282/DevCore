package com.peco2282.devcore.display

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import net.kyori.adventure.text.Component
import org.bukkit.entity.Display

class DisplayModelTest {
  @Test
  fun `line offsets descend from the anchor`() {
    assertEquals(listOf(0.0, -0.28, -0.56), hologramLineOffsets(3, 0.28))
    assertEquals(emptyList(), hologramLineOffsets(0, 0.28))
    assertFailsWith<IllegalArgumentException> { hologramLineOffsets(2, -0.1) }
  }

  @Test
  fun `builder preserves static and viewer line kinds`() {
    val spec = HologramBuilder().apply {
      line(Component.text("shared"))
      line { viewer -> Component.text(viewer.name) }
      billboard = Display.Billboard.CENTER
      lineSpacing = 0.4
      lineWidth = 120
    }.buildSpec()

    assertEquals(2, spec.lines.size)
    assertEquals(Display.Billboard.CENTER, spec.billboard)
    assertEquals(0.4, spec.lineSpacing)
    assertEquals(120, spec.lineWidth)
    assertEquals(true, spec.lines[0] is HologramLine.Static)
    assertEquals(true, spec.lines[1] is HologramLine.PerViewer)
  }
}
