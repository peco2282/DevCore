package com.peco2282.devcore.packet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PacketReflectionTest {
  @Test
  fun `reads private packet fields`() {
    val packet = TestPacket(7, "before")

    assertEquals(7, packet.getFieldValue<Int>("id"))
    assertEquals("before", packet.packetField<String>("message"))
  }

  @Test
  fun `writes private mutable packet fields`() {
    val packet = TestPacket(7, "before")

    packet.setFieldValue("message", "after")

    assertEquals("after", packet.packetField<String>("message"))
  }

  @Test
  fun `reports missing packet fields`() {
    val error = assertFailsWith<NoSuchFieldException> {
      TestPacket(7, "message").getFieldValue<Any>("missing")
    }

    assertEquals(
      "Field missing not found in ${TestPacket::class.java.name}",
      error.message
    )
  }

  private class TestPacket(
    @Suppress("unused") private val id: Int,
    @Suppress("unused") private var message: String
  )
}
