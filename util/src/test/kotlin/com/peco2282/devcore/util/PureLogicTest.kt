package com.peco2282.devcore.util

import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PureLogicTest {
  @Test
  fun `safe cast returns value or null`() {
    val value: Any = "devcore"

    assertEquals("devcore", value.asNullable<Any, String>())
    assertNull(value.asNullable<Any, Int>())
  }

  @Test
  fun `safe cast can reject incompatible value`() {
    val value: Any = "devcore"

    assertFailsWith<IllegalArgumentException> {
      value.asNullable<Any, Int>(orThrow = true)
    }
  }

  @Test
  fun `getOrDefault returns result and recovers from throwable`() {
    assertEquals(7, getOrDefault(0) { 7 })
    assertEquals(0, getOrDefault(0) { error("boom") })
  }

  @Test
  fun `vector operators return new component wise results`() {
    val first = Vector(6.0, 8.0, 10.0)
    val second = Vector(2.0, 4.0, 5.0)

    assertEquals(Vector(8.0, 12.0, 15.0), first + second)
    assertEquals(Vector(4.0, 4.0, 5.0), first - second)
    assertEquals(Vector(12.0, 32.0, 50.0), first * second)
    assertEquals(Vector(3.0, 2.0, 2.0), first / second)
    assertEquals(Vector(3.0, 4.0, 5.0), first / 2.0)
  }

  @Test
  fun `location offset leaves original location unchanged`() {
    val original = Location(null, 1.0, 2.0, 3.0)

    val shifted = original.addOffset(x = 4.0, y = -1.0, z = 0.5)

    assertEquals(Location(null, 5.0, 1.0, 3.5), shifted)
    assertEquals(Location(null, 1.0, 2.0, 3.0), original)
  }
}
