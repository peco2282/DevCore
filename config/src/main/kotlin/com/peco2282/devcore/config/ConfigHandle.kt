package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass
import kotlinx.coroutines.future.future
import kotlinx.coroutines.GlobalScope

/**
 * Handles the loading and reloading of a configuration file into an instance of type [T].
 *
 * This class provides a managed way to access configuration data, supporting automatic
 * mapping from YAML to Kotlin classes and vice-versa.
 *
 * @param T the type of the configuration class
 * @property file the [File] where the configuration is stored
 * @property clazz the [KClass] of the configuration type [T]
 */
class ConfigHandle<T : Any>(
  private val file: File,
  private val clazz: KClass<T>
) {
  /**
   * The current instance of the configuration.
   *
   * This property is initialized after the first call to [load].
   */
  lateinit var instance: T

  /**
   * Loads the configuration from the [file].
   *
   * This method reads the YAML file, maps it to an instance of type [T], and then
   * saves it back to the file to ensure all default values and comments are present.
   */
  fun load() {
    val yaml = YamlConfiguration.loadConfiguration(file)
    instance = ClassMapper.create(clazz, yaml)
    yaml.save(file)
  }

  /**
   * Loads the configuration from the [file] as a suspending function.
   *
   * This method reads the YAML file using [Dispatchers.IO], maps it to an instance
   * of type [T], and then saves it back to the file.
   *
   * @return the loaded configuration instance of type [T]
   */
  suspend fun loadSuspend(): T = withContext(Dispatchers.IO) {
    load()
    instance
  }

  /**
   * Loads the configuration from the [file] asynchronously.
   *
   * This method reads the YAML file in a background thread, maps it to an instance
   * of type [T], and then saves it back to the file.
   *
   * @return a [CompletableFuture] that completes when the configuration is loaded
   */
  @OptIn(DelicateCoroutinesApi::class)
  fun loadAsync(): CompletableFuture<T> = GlobalScope.future {
    loadSuspend()
  }

  /**
   * Reloads the configuration from the [file].
   *
   * This is an alias for [load].
   */
  fun reload() = load()

  /**
   * Reloads the configuration from the [file] as a suspending function.
   *
   * This is an alias for [loadSuspend].
   *
   * @return the reloaded configuration instance of type [T]
   */
  suspend fun reloadSuspend() = loadSuspend()

  /**
   * Reloads the configuration from the [file] asynchronously.
   *
   * This is an alias for [loadAsync].
   *
   * @return a [CompletableFuture] that completes when the configuration is reloaded
   */
  fun reloadAsync() = loadAsync()
}
