package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.reflect.KClass

object Configs {

  private val cache = mutableMapOf<Class<*>, Any>()

  inline fun <reified T : Any> load(plugin: Plugin): T {
    return load(plugin, T::class)
  }

  inline fun <reified T : Any> load(file: File): T {
    val yaml = YamlConfiguration.loadConfiguration(file)
    val instance = ClassMapper.create(T::class, yaml)

    return instance
  }

  fun <T : Any> load(plugin: Plugin, clazz: KClass<T>): T {
    val file = File(plugin.dataFolder, "config.yml")
    val yaml = YamlConfiguration.loadConfiguration(file)

    val instance = ClassMapper.create(clazz, yaml)

    cache[clazz.java] = instance
    return instance
  }

  fun reload() {
    cache.clear()
  }
}

