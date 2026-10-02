package com.peco2282.devcore.gui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SlotTest {
  @Test
  fun `converts zero based row and column to raw slot`() {
    assertEquals(0, Slot(0, 0).slot())
    assertEquals(17, Slot(1, 8).slot())
    assertEquals(53, Slot(5, 8).slot())
  }

  @Test
  fun `predefined slots cover the expected corners`() {
    assertEquals(0, Slot.SLOT_1_1.slot())
    assertEquals(8, Slot.SLOT_1_9.slot())
    assertEquals(45, Slot.SLOT_6_1.slot())
    assertEquals(53, Slot.SLOT_6_9.slot())
  }

  @Test
  fun `rejects negative coordinates`() {
    assertFailsWith<IllegalArgumentException> { Slot(-1, 0) }
    assertFailsWith<IllegalArgumentException> { Slot(0, -1) }
  }
}
