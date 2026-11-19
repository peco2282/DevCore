package com.peco2282.devcore.updatechecker

import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateCheckerTest {
  @Test fun `versions are compared numerically`() {
    assertEquals(1, compareVersions("1.21.10", "1.21.4"))
    assertEquals(0, compareVersions("v2.0.0", "2.0"))
    assertEquals(-1, compareVersions("1.9.9", "2.0.0"))
  }
}
