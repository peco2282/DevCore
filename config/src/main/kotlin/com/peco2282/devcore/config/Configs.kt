package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.reflect.KClass

/**
 * Utility for loading and managing configurations.
 *
 * This singleton provides static methods for loading configuration data from files
 * into Kotlin objects.
 */
object Configs {

  private val cache = mutableMapOf<Class<*>, Any>()

  /**
   * Loads the configuration of type [T] for the [plugin].
   *
   * The configuration file is assumed to be "config.yml" in the plugin's data folder.
   *
   * @param T the type of the configuration class
   * @param plugin the [Plugin] instance
   * @return the loaded configuration instance of type [T]
   */
  inline fun <reified T : Any> load(plugin: Plugin): T {
    return load(plugin, T::class)
  }

  /**
   * Loads the configuration of type [T] from the specified [file].
   *
   * @param T the type of the configuration class
   * @param file the configuration [File]
   * @return the loaded configuration instance of type [T]
   */
  inline fun <reified T : Any> load(file: File): T {
    val yaml = YamlConfiguration.loadConfiguration(file)
    val instance = ClassMapper.create(T::class, yaml)
    yaml.save(file) // セーブを明示的に行う
    return instance
  }

  /**
   * Loads the configuration of type [T] for the [plugin].
   *
   * The configuration file is assumed to be "config.yml" in the plugin's data folder.
   *
   * @param T the type of the configuration class
   * @param plugin the [Plugin] instance
   * @param clazz the [KClass] of the configuration type [T]
   * @return the loaded configuration instance of type [T]
   */
  fun <T : Any> load(plugin: Plugin, clazz: KClass<T>): T {
    val file = File(plugin.dataFolder, "config.yml")
    val yaml = YamlConfiguration.loadConfiguration(file)

    val instance = ClassMapper.create(clazz, yaml)
    yaml.save(file) // セーブを明示的に行う

    cache[clazz.java] = instance
    return instance
  }

  /**
   * Clears the configuration cache.
   *
   * Subsequent calls to [load] will result in re-reading the configuration from disk.
   */
  fun reload() {
    cache.clear()
  }

  /**
   * Saves the [config] object to the "config.yml" file in the [plugin]'s data folder.
   *
   * @param plugin the [Plugin] instance
   * @param config the configuration object to save
   */
  fun save(plugin: Plugin, config: Any) {
    val file = File(plugin.dataFolder, "config.yml")
    save(file, config)
  }

  /**
   * Saves the [config] object to the specified [file].
   *
   * @param file the configuration [File]
   * @param config the configuration object to save
   */
  fun save(file: File, config: Any) {
    val yaml = YamlConfiguration()
    ClassMapper.write(config, yaml)
    yaml.save(file)
  }
}

