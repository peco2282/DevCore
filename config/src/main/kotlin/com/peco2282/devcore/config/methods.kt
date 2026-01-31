package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.configuration.ConfigurationSection

/**
 * Converts this [ConfigurationSection] to an instance of type [T].
 *
 * This method uses reflection to map the keys and values in the section to the properties
 * of the class [T].
 *
 * @param T the type to convert to
 * @return an instance of type [T] populated with data from this section
 */
inline fun <reified T : Any> ConfigurationSection.convert(): T = ClassMapper.create(T::class, this)

/**
 * Gets the configuration section at the specified [key] and converts it to type [T].
 *
 * @param T the type to convert the section to
 * @param key the configuration key pointing to the section
 * @return an instance of type [T], or null if the key does not exist or is not a section
 */
inline fun <reified T : Any> ConfigurationSection.get(key: String): T? = getConfigurationSection(key)?.convert()

/**
 * Gets the configuration section at the specified [key] and converts it to type [T], or returns [default] if not found.
 *
 * @param T the type to convert the section to
 * @param key the configuration key pointing to the section
 * @param default the default value to return if the section is not found
 * @return an instance of type [T], or [default] if not found
 */
inline fun <reified T: Any> ConfigurationSection.getOrDefault(key: String, default: T): T =
  getConfigurationSection(key)?.let { ClassMapper.create(T::class, it) } ?: default

/**
 * Gets the configuration section at the specified [key], converts it to type [T], and executes the [runner] on it.
 *
 * This operator function allows for a concise DSL-like syntax for accessing and configuring nested data.
 *
 * @param T the type to convert the section to
 * @param key the configuration key pointing to the section
 * @param runner a block of code to execute on the converted instance
 * @return the converted instance of type [T], or null if the section was not found
 */
inline operator fun <reified T : Any> ConfigurationSection.get(key: String, runner: T.() -> Unit): T? {
  val cnv = this.get<T>(key)
  cnv?.runner()
  return cnv
}

