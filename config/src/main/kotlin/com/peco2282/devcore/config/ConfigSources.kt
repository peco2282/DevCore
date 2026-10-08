package com.peco2282.devcore.config

import org.bukkit.configuration.ConfigurationSection
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
    return ConfigReader(*sections.toTypedArray()).read(clazz)
  }
}
