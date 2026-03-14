package com.peco2282.devcore.config.validations

import com.peco2282.devcore.config.validations.annotations.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Engine for validating configuration objects based on annotations.
 *
 * This singleton scans the properties of a data class and applies validation
 * logic according to the annotations present on those properties.
 */
object ValidatorEngine {

  /**
   * Validates the specified [obj] based on its annotations.
   *
   * Supported annotations include [Range], [NotBlank], [Size], [Regex], [Min], [Max],
   * [Positive], [Negative], [NonNegative], [NotEmpty], [Email], [URL], and [FileExists].
   * This method also recursively validates nested data classes, lists, and maps.
   *
   * @param obj the configuration object to validate
   * @throws IllegalArgumentException if any validation rule is violated
   */
  fun validate(obj: Any) {
    if (!obj::class.isData) return

    obj::class.memberProperties.forEach { prop ->
      val value = prop.getter.call(obj)

      // 🧪 Range
      prop.findAnnotation<Range>()?.let { ann ->
        if (value is Number) {
          val v = value.toLong()
          require(v in ann.min..ann.max) {
            "Config validation failed: ${prop.name} must be between ${ann.min} and ${ann.max} (actual=$v)"
          }
        }
      }

      // 🧪 NotBlank
      prop.findAnnotation<NotBlank>()?.let {
        if (value is String) {
          require(value.isNotBlank()) {
            "Config validation failed: ${prop.name} must not be blank"
          }
        }
      }

      // 🧪 NotEmpty
      prop.findAnnotation<NotEmpty>()?.let {
        when (value) {
          is String -> require(value.isNotEmpty()) {
            "Config validation failed: ${prop.name} must not be empty"
          }

          is Collection<*> -> require(value.isNotEmpty()) {
            "Config validation failed: ${prop.name} must not be empty"
          }

          is Map<*, *> -> require(value.isNotEmpty()) {
            "Config validation failed: ${prop.name} must not be empty"
          }
        }
      }

      // 🧪 Size
      prop.findAnnotation<Size>()?.let { ann ->
        if (value is Collection<*>) {
          require(value.size in ann.min..ann.max) {
            "Config validation failed: ${prop.name} size must be ${ann.min}..${ann.max} (actual=${value.size})"
          }
        }
      }

      // 🧪 Regex
      prop.findAnnotation<Regex>()?.let { ann ->
        if (value is String) {
          require(kotlin.text.Regex(ann.pattern).matches(value)) {
            "Config validation failed: ${prop.name} must match ${ann.pattern}"
          }
        }
      }

      // 🧪 Email
      prop.findAnnotation<Email>()?.let {
        if (value is String) {
          val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
          require(emailRegex.matches(value)) {
            "Config validation failed: ${prop.name} must be a valid email address (actual=$value)"
          }
        }
      }

      prop.findAnnotation<Min>()?.let { ann ->
        if (value is Number) {
          val v = value.toLong()
          require(v >= ann.value) {
            "Config validation failed: ${prop.name} must be at least ${ann.value} (actual=$v)"
          }
        }
      }

      prop.findAnnotation<Max>()?.let { ann ->
        if (value is Number) {
          val v = value.toLong()
          require(v <= ann.value) {
            "Config validation failed: ${prop.name} must be at most ${ann.value} (actual=$v)"
          }
        }
      }

      prop.findAnnotation<Positive>()?.let {
        if (value is Number) {
          val v = value.toLong()
          require(v > 0) {
            "Config validation failed: ${prop.name} must be positive (actual=$v)"
          }
        }
      }

      prop.findAnnotation<Negative>()?.let {
        if (value is Number) {
          val v = value.toLong()
          require(v < 0) {
            "Config validation failed: ${prop.name} must be negative (actual=$v)"
          }
        }
      }

      prop.findAnnotation<NonNegative>()?.let {
        if (value is Number) {
          val v = value.toLong()
          require(v >= 0) {
            "Config validation failed: ${prop.name} must be non-negative (actual=$v)"
          }
        }
      }

      prop.findAnnotation<URL>()?.let {
        if (value is String) {
          val url = try {
            java.net.URI(value).toURL()
          } catch (_: Exception) {
            null
          }
          require(url != null) {
            "Config validation failed: ${prop.name} must be a valid URL (actual=$value)"
          }
        }
      }

      prop.findAnnotation<FileExists>()?.let {
        if (value is String) {
          require(java.io.File(value).exists()) {
            "Config validation failed: file ${value} for ${prop.name} does not exist"
          }
        }
      }


      // 🔁 ネスト data class
      if (value != null && value::class.isData) {
        validate(value)
      }

      // 🔁 List ネスト
      if (value is List<*>) {
        value.filterNotNull().forEach {
          if (it::class.isData) validate(it)
        }
      }

      // 🔁 Map ネスト
      if (value is Map<*, *>) {
        value.values.filterNotNull().forEach {
          if (it::class.isData) validate(it)
        }
      }
    }
  }
}
