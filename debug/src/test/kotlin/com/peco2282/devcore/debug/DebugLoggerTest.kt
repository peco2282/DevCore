package com.peco2282.devcore.debug
import kotlin.test.*

class DebugLoggerTest {
  @Test fun `disabled messages remain lazy and categories toggle`() {
    val lines = mutableListOf<String>(); val controller = debugController(lines::add); val log = controller.logger("storage")
    var built = false; log.debug { built = true; "x" }; assertFalse(built)
    controller.enable("storage"); log.debug { built = true; "loaded" }
    assertTrue(built); assertEquals("[storage] loaded", lines.single())
  }
  @Test fun `timed logs even when block throws`() {
    val lines = mutableListOf<String>(); val controller = debugController(lines::add); controller.enable("x")
    assertFails { controller.logger("x").timed("boom") { error("failure") } }
    assertTrue(lines.single().contains("boom took"))
  }
}
