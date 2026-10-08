package com.peco2282.devcore.config.reflection

import com.peco2282.devcore.config.validations.annotations.Clamp
import com.peco2282.devcore.config.validations.annotations.ClampAtLeast
import com.peco2282.devcore.config.validations.annotations.ClampAtMost
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

/** Applies normalization annotations to values before constructor invocation. */
internal object ValueNormalizer {

  fun normalize(parameter: KParameter, value: Any?): Any? {
    if (value == null) return null

    val clamp = parameter.findAnnotation<Clamp>()
    val clampAtLeast = parameter.findAnnotation<ClampAtLeast>()
    val clampAtMost = parameter.findAnnotation<ClampAtMost>()
    if (clamp == null && clampAtLeast == null && clampAtMost == null) return value

    require(value is Number) {
      "Config normalization failed: ${parameter.name} uses a clamp annotation but is not numeric"
    }

    val minimum = maxOf(
      clamp?.min ?: Double.NEGATIVE_INFINITY,
      clampAtLeast?.value ?: Double.NEGATIVE_INFINITY
    )
    val maximum = minOf(
      clamp?.max ?: Double.POSITIVE_INFINITY,
      clampAtMost?.value ?: Double.POSITIVE_INFINITY
    )

    require(!minimum.isNaN() && !maximum.isNaN() && minimum <= maximum) {
      "Config normalization failed: ${parameter.name} has invalid clamp bounds ($minimum..$maximum)"
    }

    return when (val classifier = parameter.type.classifier as? KClass<*>) {
      Double::class -> value.toDouble().coerceIn(minimum, maximum)
      Float::class -> value.toDouble().coerceIn(
        minimum.coerceIn(-Float.MAX_VALUE.toDouble(), Float.MAX_VALUE.toDouble()),
        maximum.coerceIn(-Float.MAX_VALUE.toDouble(), Float.MAX_VALUE.toDouble())
      ).toFloat()
      Long::class -> value.toLong().coerceIn(integralMinimum(minimum), integralMaximum(maximum))
      Int::class -> value.toInt().coerceIn(
        integralMinimum(minimum).coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt(),
        integralMaximum(maximum).coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
      )
      Short::class -> value.toInt().coerceIn(
        integralMinimum(minimum).coerceIn(Short.MIN_VALUE.toLong(), Short.MAX_VALUE.toLong()).toInt(),
        integralMaximum(maximum).coerceIn(Short.MIN_VALUE.toLong(), Short.MAX_VALUE.toLong()).toInt()
      ).toShort()
      Byte::class -> value.toInt().coerceIn(
        integralMinimum(minimum).coerceIn(Byte.MIN_VALUE.toLong(), Byte.MAX_VALUE.toLong()).toInt(),
        integralMaximum(maximum).coerceIn(Byte.MIN_VALUE.toLong(), Byte.MAX_VALUE.toLong()).toInt()
      ).toByte()
      else -> throw IllegalArgumentException(
        "Config normalization failed: ${parameter.name} has unsupported numeric type $classifier"
      )
    }
  }

  private fun integralMinimum(value: Double): Long = when {
    value == Double.NEGATIVE_INFINITY -> Long.MIN_VALUE
    value >= Long.MAX_VALUE.toDouble() -> Long.MAX_VALUE
    value <= Long.MIN_VALUE.toDouble() -> Long.MIN_VALUE
    else -> ceil(value).toLong()
  }

  private fun integralMaximum(value: Double): Long = when {
    value == Double.POSITIVE_INFINITY -> Long.MAX_VALUE
    value >= Long.MAX_VALUE.toDouble() -> Long.MAX_VALUE
    value <= Long.MIN_VALUE.toDouble() -> Long.MIN_VALUE
    else -> floor(value).toLong()
  }
}
