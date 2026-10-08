package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import kotlin.reflect.KClass

/**
 * An ordered collection of configuration sections.
 *
 * Earlier sections have higher priority. Missing values fall through to later
 * sections, and values missing from every section use the mapped class's
 * constructor defaults.
 */
class ConfigSources internal constructor(
  private val sections: List<ConfigurationSection>
) {

  /**
   * Converts the layered sections to an instance of [T].
   */
  inline fun <reified T : Any> convert(): T = convert(T::class)

  /**
   * Converts the layered sections to an instance of [clazz].
   */
  fun <T : Any> convert(clazz: KClass<T>): T {
    val merged = YamlConfiguration()

    // Apply low-priority sections first, then overlay higher-priority values.
    sections.asReversed().forEach { source ->
      overlay(merged, source)
    }

    return ClassMapper.create(clazz, merged)
  }

  private fun overlay(target: ConfigurationSection, source: ConfigurationSection) {
    source.getKeys(false).forEach { key ->
      val sourceSection = source.getConfigurationSection(key)
      if (sourceSection != null) {
        val targetSection = target.getConfigurationSection(key) ?: run {
          target.set(key, null)
          target.createSection(key)
        }
        overlay(targetSection, sourceSection)
      } else {
        target.set(key, source.get(key))
      }
    }
  }
}
