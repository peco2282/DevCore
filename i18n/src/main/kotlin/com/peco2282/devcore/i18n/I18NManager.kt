package com.peco2282.devcore.i18n

import org.bukkit.plugin.Plugin
import java.io.File


/**
 * Manager for I18N instances.
 * 
 * This singleton object manages I18N instances for different plugins,
 * ensuring that each plugin has at most one I18N instance associated with it.
 */
object I18NManager {
  private val i18nMap = mutableMapOf<String, I18N>()

  /**
   * Creates or retrieves an I18N instance for the specified plugin.
   * 
   * @param plugin The plugin for which to create the I18N instance
   * @param dir The directory containing locale files
   * @return The I18N instance associated with the plugin
   */
  fun create(plugin: Plugin, dir: File): I18N = i18nMap.getOrPut(plugin.name) { I18N(plugin, dir) }

  /**
   * Gets the I18N instance for the specified plugin.
   * 
   * If no instance exists, creates a new one with the default directory
   * (plugin's data folder + "i18n").
   * 
   * @param plugin The plugin for which to get the I18N instance
   * @return The I18N instance associated with the plugin
   */
  fun get(plugin: Plugin, dir: File = plugin.dataFolder.resolve("i18n")): I18N = i18nMap[plugin.name] ?: create(
    plugin,
    dir
  )

  /**
   * Removes the I18N instance associated with the specified plugin.
   * 
   * @param plugin The plugin whose I18N instance should be removed
   */
  fun remove(plugin: Plugin) {
    i18nMap.remove(plugin.name)
  }

  /**
   * Reloads the locales for the I18N instance associated with the specified plugin.
   * 
   * @param plugin The plugin whose I18N instance should be reloaded
   */
  fun reload(plugin: Plugin) {
    i18nMap[plugin.name]?.loadLocales()
  }
}