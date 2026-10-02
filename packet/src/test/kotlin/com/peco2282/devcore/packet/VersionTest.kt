package com.peco2282.devcore.packet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionTest {
  @Test
  fun `parses complete and abbreviated versions`() {
    assertEquals(Version("1.20.6"), Version.parse("1.20.6"))
    assertEquals(Version("1.21.0"), Version("1.21"))
    assertEquals("v1_21_4", Version("1.21.4").packageString())
    assertEquals("1.21.4", Version("1.21.4").toString())
  }

  @Test
  fun `compares major minor and patch components`() {
    assertTrue(Version("2.0.0") > Version("1.99.99"))
    assertTrue(Version("1.21.0") > Version("1.20.6"))
    assertTrue(Version("1.20.6") > Version("1.20.5"))
    assertEquals(0, Version("1.20.6").compareTo(Version("1.20.6")))
  }

  @Test
  fun `supports closed and open ended string ranges`() {
    assertTrue("1.20.6" in "1.20.4".."1.20.6")
    assertTrue("1.20.5" in "1.20.4"..<"1.20.6")
    assertFalse("1.20.6" in "1.20.4"..<"1.20.6")
  }

  @Test
  fun `rejects malformed versions`() {
    assertFailsWith<IllegalArgumentException> { Version("1") }
    assertFailsWith<IllegalArgumentException> { Version("x.20.4") }
    assertFailsWith<IllegalArgumentException> { Version("1.x.4") }
  }
}
