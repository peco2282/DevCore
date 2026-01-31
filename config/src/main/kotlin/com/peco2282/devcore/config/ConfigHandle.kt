package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.reflect.KClass

class ConfigHandle<T : Any>(
  private val file: File,
  private val clazz: KClass<T>
) {
  lateinit var instance: T

  fun load() {
    val yaml = YamlConfiguration.loadConfiguration(file)
    instance = ClassMapper.create(clazz, yaml)
    yaml.save(file)
  }

  fun reload() = load()
}
