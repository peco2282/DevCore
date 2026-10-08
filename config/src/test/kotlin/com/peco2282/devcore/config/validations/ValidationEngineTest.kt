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

  data class DecimalConfig(
    @Positive val positive: Double = 0.5,
    @Negative val negative: Double = -0.5,
    @NonNegative val nonNegative: Float = 0.25f,
    @Range(min = -1, max = 1) val ranged: Double = 0.5,
    @Min(0) val minimum: Double = 0.5,
    @Max(0) val maximum: Double = -0.5
  )

  @Test
  fun `fractional numeric values are validated without truncation`() {
    assertDoesNotThrow {
      ValidatorEngine.validate(DecimalConfig())
    }
  }

  @Test
  fun `fractional values outside numeric constraints are rejected`() {
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(DecimalConfig(positive = -0.1))
    }
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(DecimalConfig(negative = 0.1))
    }
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(DecimalConfig(ranged = 1.1))
    }
  }

  data class FiniteConfig(
    @Finite val value: Double
  )

  @Test
  fun `finite rejects NaN and infinity`() {
    assertDoesNotThrow {
      ValidatorEngine.validate(FiniteConfig(1.0))
    }
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(FiniteConfig(Double.NaN))
    }
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(FiniteConfig(Double.POSITIVE_INFINITY))
    }
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(FiniteConfig(Double.NEGATIVE_INFINITY))
    }
  }

  data class IntervalConfig(
    val minimum: Double,
    val maximum: Double
  ) : ValidatableConfig {
    override fun validate() {
      require(minimum <= maximum) {
        "minimum must not exceed maximum"
      }
    }
  }

  data class ContainerConfig(
    val interval: IntervalConfig
  )

  @Test
  fun `custom validation runs recursively`() {
    assertDoesNotThrow {
      ValidatorEngine.validate(ContainerConfig(IntervalConfig(1.0, 2.0)))
    }
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(ContainerConfig(IntervalConfig(2.0, 1.0)))
    }
  }

  class PlainConfig(
    private val valid: Boolean
  ) : ValidatableConfig {
    override fun validate() {
      require(valid) { "config must be valid" }
    }
  }

  @Test
  fun `custom validation supports non-data classes`() {
    assertDoesNotThrow {
      ValidatorEngine.validate(PlainConfig(true))
    }
    assertThrows<IllegalArgumentException> {
      ValidatorEngine.validate(PlainConfig(false))
    }
  }
}
