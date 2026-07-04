package com.peco2282.devcore.i18n

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.File
import java.text.MessageFormat
import java.util.*


/**
 * Internationalization (I18N) manager for Bukkit plugins.
 * 
 * This class handles loading and managing localized messages from YAML files.
 * Each file represents a locale and contains key-value pairs for translations.
 * 
 * @param plugin The Bukkit plugin instance
 * @param dir The directory containing locale files
 * @param fileValidator A predicate to validate locale files (default: checks for .yml or .yaml extension)
 */
class I18N(
  val plugin: Plugin,
  val dir: File,
  val fileValidator: (File) -> Boolean = { it.extension == "yml" || it.extension == "yaml" }
) {
  private val locales = mutableMapOf<Locale, Properties>()

  /**
   * The default locale to use when a translation is not found in the requested locale.
   * Defaults to Locale.US (English - United States).
   */
  var defaultLocale: Locale = Locale.US

  init {
    if (!dir.exists()) dir.mkdirs()
    loadLocales()
  }

  /**
   * Converts a locale string to a Locale object.
   * 
   * Supports formats like "en", "en_US", or "en_US_variant".
   * Automatically normalizes hyphens to underscores.
   * 
   * @param localeStr The locale string to convert
   * @return The corresponding Locale object
   */
  private fun asLocale(localeStr: String): Locale {
    val normalized = localeStr.replace("-", "_")
    val parts = normalized.split("_")
    return when (parts.size) {
      1 -> Locale.of(parts[0])
      2 -> Locale.of(parts[0], parts[1])
      3 -> Locale.of(parts[0], parts[1], parts[2])
      else -> Locale.of(normalized)
    }
  }

  /**
   * Loads all locale files from the configured directory.
   * 
   * Clears existing locales and reloads from YAML files.
   * Only primitive types (String, Number, Boolean, Char) are loaded as properties.
   * File names (without extension) are used to determine the locale.
   */
  fun loadLocales() {
    locales.clear()
    dir.listFiles(fileValidator)?.forEach { file ->
      val locale = asLocale(file.nameWithoutExtension)
      val properties = locales.getOrPut(locale) { Properties() }
      val yml = YamlConfiguration.loadConfiguration(file)
      yml.getKeys(true).forEach { key ->
        val value = yml.get(key) ?: return@forEach
        if (value is String || value is Number || value is Boolean || value is Char) {
          properties.setProperty(key, value.toString())
        }
      }
    }
  }

  /**
   * Translates a message key for the specified locale.
   * 
   * Falls back to the default locale if the key is not found.
   * Supports MessageFormat-style placeholders (e.g., {0}, {1}).
   * 
   * @param locale The target locale
   * @param key The translation key
   * @param args Optional arguments for placeholder substitution
   * @return The translated message, or null if the key is not found
   */
  fun translate(locale: Locale, key: String, vararg args: Any): String? {
    val properties = locales[locale] ?: locales[defaultLocale] ?: return null
    val message = properties.getProperty(key) ?: return null
    if (args.isEmpty()) return message
    return try {
      MessageFormat.format(message, *args)
    } catch (e: Exception) {
      message
    }
  }

  /**
   * Translates a message key using the player's locale.
   * 
   * @param player The player whose locale will be used
   * @param key The translation key
   * @param args Optional arguments for placeholder substitution
   * @return The translated message, or null if the key is not found
   */
  fun translate(player: Player, key: String, vararg args: Any): String? = translate(player.locale(), key, *args)

  /**
   * Saves default locale files from the plugin's resources.
   * 
   * Automatically reloads locales after saving.
   * 
   * @param resourcePath The path within the plugin's resources where locale files are located (default: "i18n")
   * @param locales List of locale identifiers (e.g., ["en_US", "ja_JP"])
   * @param replace Whether to replace existing files (default: false)
   */
  fun saveDefaultLocaleFiles(resourcePath: String = "i18n", locales: List<String>, replace: Boolean = false) {
    locales.forEach { locale ->
      val fileName = "$locale.yml"
      val path = if (resourcePath.isEmpty()) fileName else "$resourcePath/$fileName"
      if (plugin.getResource(path) != null) {
        plugin.saveResource(path, replace)
      }
    }
    loadLocales()
  }
}
