package com.peco2282.devcore.config

import com.peco2282.devcore.config.Configs.load
import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.nio.file.*
import kotlin.reflect.KClass

/**
 * Utility for loading and managing configurations.
 *
 * This singleton provides static methods for loading configuration data from files
 * into Kotlin objects.
 */
object Configs {

  private val cache = mutableMapOf<Class<*>, Any>()
  private val watchers = mutableMapOf<File, WatchKey>()
  private val watchService by lazy { FileSystems.getDefault().newWatchService() }

  /**
   * Loads the configuration of type [T] for the [plugin].
   *
   * The configuration file is assumed to be "config.yml" in the plugin's data folder.
   *
   * @param T the type of the configuration class
   * @param plugin the [Plugin] instance
   * @param autoReload whether to automatically reload the config when the file changes
   * @param onReload a callback function to execute when the config is reloaded
   * @return the loaded configuration instance of type [T]
   */
  inline fun <reified T : Any> load(
    plugin: Plugin,
    autoReload: Boolean = false,
    noinline onReload: (T) -> Unit = {}
  ): T {
    return load(plugin, T::class, autoReload, onReload)
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
    yaml.save(file) // Explicitly perform save
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
   * @param autoReload whether to automatically reload the config when the file changes
   * @param onReload a callback function to execute when the config is reloaded
   * @return the loaded configuration instance of type [T]
   */
  fun <T : Any> load(
    plugin: Plugin,
    clazz: KClass<T>,
    autoReload: Boolean = false,
    onReload: (T) -> Unit = {}
  ): T {
    val file = File(plugin.dataFolder, "config.yml")
    if (!file.exists()) {
      plugin.saveResource("config.yml", false)
    }
    val yaml = YamlConfiguration.loadConfiguration(file)

    val instance = ClassMapper.create(clazz, yaml)
    yaml.save(file) // Explicitly perform save

    cache[clazz.java] = instance

    if (autoReload && !watchers.containsKey(file)) {
      startWatcher(plugin, file, clazz, onReload)
    }

    return instance
  }

  private fun <T : Any> startWatcher(
    plugin: Plugin,
    file: File,
    clazz: KClass<T>,
    onReload: (T) -> Unit
  ) {
    val path = file.parentFile.toPath()
    val key = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
    watchers[file] = key

    Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
      while (watchers.containsKey(file)) {
        val watchKey = watchService.take()
        for (event in watchKey.pollEvents()) {
          val context = event.context() as Path
          if (context.toString() == file.name) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
              val newInstance = load(plugin, clazz)
              onReload(newInstance)
            })
          }
        }
        if (!watchKey.reset()) {
          watchers.remove(file)
          break
        }
      }
    })
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

