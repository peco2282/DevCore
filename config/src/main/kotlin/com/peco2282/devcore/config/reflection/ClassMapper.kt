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

object ClassMapper {

  fun <T : Any> create(clazz: KClass<T>, section: ConfigurationSection): T {
    val ctor = clazz.primaryConstructor!!
    val args = mutableMapOf<KParameter, Any?>()

    for (param in ctor.parameters) {
      val name = param.name!!
      val type = param.type

      if (section.contains(name)) {
        val value = FieldResolver.resolve(section, name, type)
        if (value != null) {
          args[param] = value
        }
      }
    }

    val instance = ctor.callBy(args)
    ValidatorEngine.validate(instance)

    // 🔥 常に現在の正しい状態を書き出す
    writeFully(instance, section)

    return instance
  }

  private fun writeFully(obj: Any, section: ConfigurationSection) {
    obj::class.memberProperties.forEach { prop ->
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
              else -> element
            }
          }
          section.set(prop.name, listToSave)
        }

        value::class.isData -> {
          val sub = section.getConfigurationSection(prop.name)
            ?: section.createSection(prop.name)
          writeFully(value, sub)
        }

        value is Map<*, *> -> {
          if (!section.contains(prop.name)) {
            section.createSection(prop.name)
          }
          comment?.let { section.setComments(prop.name, listOf(it)) }
          value.forEach { (k, v) ->
            if (k !is String || v == null) return@forEach
            val path = "${prop.name}.$k"

            if (v::class.isData) {
              val sub = section.getConfigurationSection(path) ?: section.createSection(path)
              writeFully(v, sub)
            } else {
              if (!section.contains(path)) {
                section.set(path, v)
              }
            }
          }
        }


        else -> section.set(prop.name, value)
      }
    }
  }

  private fun dataClassToMap(obj: Any): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    obj::class.memberProperties.forEach { prop ->
      val value = prop.getter.call(obj)
      map[prop.name] =
        if (value != null && value::class.isData) dataClassToMap(value)
        else value
    }
    return map
  }
}
