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
   * [Positive], [URL], and [FileExists]. This method also recursively validates
   * nested data classes, lists, and maps.
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

      prop.findAnnotation<Min>()?.let {
        require((value as Number).toLong() >= it.value)
      }

      prop.findAnnotation<Max>()?.let {
        require((value as Number).toLong() <= it.value)
      }

      prop.findAnnotation<Positive>()?.let {
        require((value as Number).toLong() > 0)
      }

      prop.findAnnotation<URL>()?.let {
        val url = try {
          java.net.URI(value as String).toURL()
        } catch (_: Exception) {
          null
        }
        require(url != null)
      }

      prop.findAnnotation<FileExists>()?.let {
        require(java.io.File(value as String).exists())
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
