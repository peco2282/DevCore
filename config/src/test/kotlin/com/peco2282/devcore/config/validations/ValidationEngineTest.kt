package com.peco2282.devcore.config.validations

import com.peco2282.devcore.config.validations.annotations.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ValidationEngineTest {

  data class TestConfig(
    @Negative val negativeVal: Int = -1,
    @NonNegative val nonNegativeVal: Int = 0,
    @NotEmpty val notEmptyString: String = "not empty",
    @NotEmpty val notEmptyList: List<String> = listOf("item"),
    @Email val email: String = "test@example.com"
  )

  @Test
  fun testValidConfig() {
    val config = TestConfig()
    assertDoesNotThrow {
      ValidatorEngine.validate(config)
    }
  }

  @Test
  fun testNegativeInvalid() {
    val config = TestConfig(negativeVal = 0)
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(config)
    }
  }

  @Test
  fun testNonNegativeInvalid() {
    val config = TestConfig(nonNegativeVal = -1)
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(config)
    }
  }

  @Test
  fun testNotEmptyStringInvalid() {
    val config = TestConfig(notEmptyString = "")
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(config)
    }
  }

  @Test
  fun testNotEmptyListInvalid() {
    val config = TestConfig(notEmptyList = emptyList())
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(config)
    }
  }

  @Test
  fun testEmailInvalid() {
    val config = TestConfig(email = "invalid-email")
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(config)
    }
  }

  data class NestedConfig(
    @NotBlank val name: String = "nested",
    val inner: TestConfig = TestConfig()
  )

  @Test
  fun testNestedValidation() {
    val config = NestedConfig(inner = TestConfig(negativeVal = 10))
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(config)
    }
  }
}
