package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin

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

/**
 * Loads and returns the plugin's configuration as an instance of type [C].
 *
 * This extension function is a convenient shorthand for calling [Configs.load] with this plugin instance.
 * It automatically loads the "config.yml" file from the plugin's data folder and maps it to the specified
 * configuration class type using reflection.
 *
 * The configuration class [C] should be a data class with properties matching the structure of the YAML file.
 * Default values in the data class constructor will be used for any missing fields in the config.
 *
 * @param C the type of the configuration class to load
 * @return an instance of type [C] populated with data from the plugin's config.yml
 *
 * @see Configs.load
 * @see convert
 *
 * Example usage:
 * ```kotlin
 * data class MyConfig(val enabled: Boolean = true, val message: String = "Hello")
 *
 * class MyPlugin : JavaPlugin() {
 *     lateinit var config: MyConfig
 *
 *     override fun onEnable() {
 *         saveDefaultConfig()
 *         config = getConfigInstance()
 *     }
 * }
 * ```
 */
inline fun <reified C: Any> Plugin.getConfigInstance(): C {
  return Configs.load<C>(this)
}
