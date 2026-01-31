package com.peco2282.devcore.config.reflection

import org.bukkit.configuration.ConfigurationSection
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

object FieldResolver {

  fun resolve(section: ConfigurationSection, path: String, type: KType): Any? {
    val classifier = type.classifier as KClass<*>

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
          if (argType.classifier is KClass<*> && (argType.classifier as KClass<*>).isData) {
            val map = element as Map<String, Any?>
            mapToDataClass(argType.classifier as KClass<Any>, map)
          } else element
        }
      }

      classifier == Map::class -> {
        val keyType = type.arguments[0].type!!
        val valueType = type.arguments[1].type!!
        val valueClass = valueType.classifier as KClass<*>

        val sectionMap = section.getConfigurationSection(path) ?: return emptyMap<String, Any>()

        sectionMap.getKeys(false).associateWith { key ->
          if (valueClass.isData) {
            val sub = sectionMap.getConfigurationSection(key)!!
            mapSectionToDataClass(valueClass, sub)
          } else {
            sectionMap.get(key)
          }
        }
      }

      classifier.java.isEnum ->{
        val raw = section.getString(path) ?: return null
        java.lang.Enum.valueOf(classifier.java as Class<out Enum<*>>, raw.uppercase())
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

  private fun <T : Any> mapSectionToDataClass(clazz: KClass<T>, section: ConfigurationSection): T {
    val ctor = clazz.primaryConstructor!!
    val args = mutableMapOf<KParameter, Any?>()

    for (param in ctor.parameters) {
      val name = param.name!!

      // 🔥 ここが重要
      if (!section.contains(name)) continue

      val value = resolve(section, name, param.type)
      args[param] = value
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
