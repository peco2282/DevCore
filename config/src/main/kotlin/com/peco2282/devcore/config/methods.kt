package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.configuration.ConfigurationSection

inline fun <reified T : Any> ConfigurationSection.convert(): T = ClassMapper.create(T::class, this)

inline fun <reified T : Any> ConfigurationSection.get(key: String): T? = getConfigurationSection(key)?.convert()

inline fun <reified T: Any> ConfigurationSection.getOrDefault(key: String, default: T): T =
  getConfigurationSection(key)?.let { ClassMapper.create(T::class, it) } ?: default

inline operator fun <reified T : Any> ConfigurationSection.get(key: String, runner: T.() -> Unit): T? {
  val cnv = this.get<T>(key)
  cnv?.runner()
  return cnv
}

