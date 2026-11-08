package com.peco2282.devcore.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

interface Greeter { fun hi(): String }
class OldGreeter : Greeter { override fun hi() = "old" }
class NewGreeter : Greeter { override fun hi() = "new" }

class VersionedTest {
  private fun v(s: String) = MinecraftVersion.parse(s)

  @Test
  fun `compares numerically not lexicographically`() {
    assertTrue(v("1.21.10") > v("1.21.4"))
    assertTrue(v("1.21.11") > v("1.21.3"))
    assertTrue("1.21.10" < "1.21.4") // the string comparison this class exists to avoid
  }

  @Test
  fun `parses short versions and suffixes`() {
    assertEquals(v("1.21.0"), v("1.21"))
    assertEquals(v("1.21.4"), v("1.21.4-rc1"))
    assertFailsWith<IllegalArgumentException> { v("1") }
    assertFailsWith<IllegalArgumentException> { v("x.21") }
  }

  private val impls = versioned<Greeter>("greeter") {
    since("1.21.4", NewGreeter::class.java.name)
    since("1.20.6", OldGreeter::class.java.name) // order of declaration must not matter
    supportedBefore("1.22")
  }

  @Test
  fun `picks the latest entry not newer than the version`() {
    assertEquals(OldGreeter::class.java.name, impls.classNameFor(v("1.20.6")))
    assertEquals(OldGreeter::class.java.name, impls.classNameFor(v("1.21.3")))
    assertEquals(NewGreeter::class.java.name, impls.classNameFor(v("1.21.4")))
    assertEquals(NewGreeter::class.java.name, impls.classNameFor(v("1.21.10")))
  }

  @Test
  fun `too old and too new are unsupported`() {
    assertNull(impls.classNameFor(v("1.20.5")))
    assertNull(impls.classNameFor(v("1.22")))
  }

  @Test
  fun `without an upper bound newer versions use the latest entry`() {
    val open = versioned<Greeter>("open") { since("1.21.4", NewGreeter::class.java.name) }
    assertEquals(NewGreeter::class.java.name, open.classNameFor(v("1.30.0")))
  }

  @Test
  fun `load instantiates and unsupported versions fail with a clear message`() {
    assertEquals("new", impls.load(v("1.21.5")).hi())
    val e = assertFailsWith<IllegalStateException> { impls.load(v("1.19.4")) }
    assertTrue("greeter" in e.message!! && "1.19.4" in e.message!!)
  }

  @Test
  fun `missing class is reported with the class name`() {
    val broken = versioned<Greeter>("broken") { since("1.0", "does.not.Exist") }
    val e = assertFailsWith<IllegalStateException> { broken.load(v("1.21.4")) }
    assertTrue("does.not.Exist" in e.message!!)
  }
}
