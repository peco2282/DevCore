package com.peco2282.devcore.config.reflection

import org.bukkit.configuration.ConfigurationSection
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

/**
 * Utility for resolving fields from configuration sections.
 *
 * This singleton provides logic for extracting values of various types (primitives,
 * lists, maps, enums, and data classes) from a [ConfigurationSection].
 */
object FieldResolver {

  /**
   * Resolves the value at the specified [path] from the [section] as the given [type].
   *
   * @param section the [ConfigurationSection] to read from
   * @param path the configuration path to the value
   * @param type the [KType] of the value to resolve
   * @return the resolved value, or null if it cannot be resolved
   */
  fun resolve(section: ConfigurationSection, path: String, type: KType): Any? {
    val classifier = type.classifier as? KClass<*> ?: return section.get(path)

    if (TypeSerializers.has(classifier)) {
      return TypeSerializers.deserialize(classifier as KClass<Any>, section.get(path))
    }

    return when {
      classifier == String::class -> section.getString(path)
      classifier == Int::class -> section.getInt(path)
      classifier == Boolean::class -> section.getBoolean(path)
      classifier == Double::class -> section.getDouble(path)

      classifier == List::class -> {
        val argType = type.arguments.first().type!!
        val list = section.getList(path) ?: return emptyList<Any>()

        @Suppress("UNCHECKED_CAST")
        list.map { element ->
          resolveElement(element, argType)
        }
      }

      classifier == Map::class -> {
        val valueType = type.arguments[1].type!!
        val valueClass = valueType.classifier as KClass<*>

        val sectionMap = section.getConfigurationSection(path) ?: return emptyMap<String, Any>()

        sectionMap.getKeys(false).associateWith { key ->
          if (valueClass.isData) {
            val sub = sectionMap.getConfigurationSection(key)!!
            mapSectionToDataClass(valueClass, sub)
          } else {
            val element = sectionMap.get(key)
            resolveElement(element, valueType)
          }
        }
      }

      classifier.java.isEnum ->{
        val raw = section.get(path) ?: return null
        val name = raw.toString().uppercase()
        java.lang.Enum.valueOf(classifier.java as Class<out Enum<*>>, name)
      }


      classifier.isData -> {
        val sub = section.getConfigurationSection(path) ?: return null
        mapSectionToDataClass(classifier, sub)
      }

      type.isMarkedNullable && !section.contains(path) -> {
        null
      }


      else -> section.get(path)
    }
  }

  private fun resolveElement(element: Any?, type: KType): Any? {
    if (element == null) return null
    val classifier = type.classifier as? KClass<*> ?: return element

    return when {
      classifier.isData -> {
        val map = element as? Map<String, Any?> ?: return element
        mapToDataClass(classifier as KClass<Any>, map)
      }
      classifier == List::class -> {
        val argType = type.arguments.first().type!!
        val list = element as? List<*> ?: return element
        list.map { resolveElement(it, argType) }
      }
      classifier == Map::class -> {
        val valueType = type.arguments[1].type!!
        val map = element as? Map<String, Any?> ?: return element
        map.mapValues { resolveElement(it.value, valueType) }
      }
      else -> TypeSerializers.deserializeOrRaw(classifier as KClass<Any>, element)
    }
  }

  private fun <T : Any> mapSectionToDataClass(clazz: KClass<T>, section: ConfigurationSection): T {
    val ctor = clazz.primaryConstructor!!
    val args = mutableMapOf<KParameter, Any?>()

    for (param in ctor.parameters) {
      val name = param.name!!

      if (!section.contains(name)) continue

      val value = resolve(section, name, param.type)
      if (value != null || param.type.isMarkedNullable) {
        args[param] = value
      }
    }

    return ctor.callBy(args)
  }

  private fun <T : Any> mapToDataClass(clazz: KClass<T>, map: Map<String, Any?>): T {
    val ctor = clazz.primaryConstructor!!
    val args = mutableMapOf<KParameter, Any?>()

    for (param in ctor.parameters) {
      val name = param.name!!
      if (!map.containsKey(name)) continue

      args[param] = map[name]
    }

    return ctor.callBy(args)
  }
}
