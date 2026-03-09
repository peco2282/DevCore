package com.peco2282.devcore.config.reflection

import com.peco2282.devcore.config.validations.ValidatorEngine
import com.peco2282.devcore.config.validations.annotations.Comment
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Utility for mapping configuration sections to Kotlin classes.
 *
 * This singleton provides methods to instantiate Kotlin classes from Bukkit's
 * [ConfigurationSection] and to write object properties back to a section.
 */
object ClassMapper {

  /**
   * Creates an instance of type [T] from the [section].
   *
   * This method uses the primary constructor of the class and resolves each parameter
   * using [FieldResolver]. It also performs validation using [ValidatorEngine] and
   * writes the final state back to the section using [write].
   *
   * @param T the type of the class to instantiate
   * @param clazz the [KClass] of type [T]
   * @param section the [ConfigurationSection] to read data from
   * @return a new instance of type [T]
   */
  fun <T : Any> create(clazz: KClass<T>, section: ConfigurationSection): T {
    val ctor = clazz.primaryConstructor!!
    val args = mutableMapOf<KParameter, Any?>()

    for (param in ctor.parameters) {
      val name = param.name!!
      val type = param.type

      if (section.contains(name)) {
        val value = FieldResolver.resolve(section, name, type)
        if (value != null || type.isMarkedNullable) {
          args[param] = value
        }
      }
    }

    val instance = ctor.callBy(args)
    ValidatorEngine.validate(instance)

    // 🔥 常に現在の正しい状態を書き出す
    write(instance, section)

    return instance
  }

  /**
   * Writes the properties of the [obj] back to the [section].
   *
   * @param obj the object to write
   * @param section the [ConfigurationSection] to write to
   */
  fun write(obj: Any, section: ConfigurationSection) {
    val clazz = obj::class
    val comment = clazz.findAnnotation<Comment>()?.text
    if (comment != null) {
      section.setComments("", listOf(comment))
    }

    clazz.memberProperties.forEach { prop ->
      val value = prop.getter.call(obj) ?: return@forEach

      val comment = prop.findAnnotation<Comment>()?.text
      if (comment != null) {
        section.setComments(prop.name, listOf(comment))
      }

      when {
        value is List<*> -> {
          val listToSave = value.map { element ->
            when {
              element == null -> null
              element::class.isData -> dataClassToMap(element)
              element is List<*> -> serializeList(element)
              element is Map<*, *> -> serializeMap(element)
              else -> TypeSerializers.serializeOrRaw(element)
            }
          }
          section.set(prop.name, listToSave)
        }

        value::class.isData -> {
          val sub = section.getConfigurationSection(prop.name)
            ?: section.createSection(prop.name)
          write(value, sub)
        }

        value is Map<*, *> -> {
          if (!section.contains(prop.name)) {
            section.createSection(prop.name)
          }
          comment?.let { section.setComments(prop.name, listOf(it)) }
          value.forEach { (k, v) ->
            if (k !is String || v == null) return@forEach
            val path = "${prop.name}.$k"

            when {
              v::class.isData -> {
                val sub = section.getConfigurationSection(path) ?: section.createSection(path)
                write(v, sub)
              }
              v is List<*> -> {
                section.set(path, serializeList(v))
              }
              v is Map<*, *> -> {
                section.set(path, serializeMap(v))
              }
              else -> {
                section.set(path, TypeSerializers.serializeOrRaw(v))
              }
            }
          }
        }


        else -> {
          val serialized = TypeSerializers.serializeOrRaw(value)
          section.set(prop.name, serialized)
        }
      }
    }
  }

  private fun dataClassToMap(obj: Any): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    val clazz = obj::class
    clazz.memberProperties.forEach { prop ->
      val value = prop.getter.call(obj)
      map[prop.name] =
        if (value != null) {
          when {
            value::class.isData -> dataClassToMap(value)
            value is List<*> -> serializeList(value)
            value is Map<*, *> -> serializeMap(value)
            else -> TypeSerializers.serializeOrRaw(value)
          }
        } else null
    }
    return map
  }

  private fun serializeList(list: List<*>): List<Any?> {
    return list.map { element ->
      if (element == null) return@map null
      when {
        element::class.isData -> dataClassToMap(element)
        element is List<*> -> serializeList(element)
        element is Map<*, *> -> serializeMap(element)
        else -> TypeSerializers.serializeOrRaw(element)
      }
    }
  }

  private fun serializeMap(map: Map<*, *>): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    map.forEach { (k, v) ->
      if (k !is String || v == null) return@forEach
      result[k] = when {
        v::class.isData -> dataClassToMap(v)
        v is List<*> -> serializeList(v)
        v is Map<*, *> -> serializeMap(v)
        else -> TypeSerializers.serializeOrRaw(v)
      }
    }
    return result
  }
}
