package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.reflect.KClass

/**
 * Configurable reader for files, plugin configuration files, and layered sections.
 *
 * File-backed readers write constructor defaults and normalized values back by
 * default. Section-backed readers merge into an isolated in-memory configuration
 * and never mutate their source sections.
 */
class ConfigReader private constructor(
  private val input: Input
) {

  private var writeDefaults: Boolean = input is Input.FileInput
  private var sectionPath: String? = null
  private var useRootFallback: Boolean = false
  private val fallbackSections = mutableListOf<FallbackSection>()

  /** Creates a reader backed by [file]. */
  constructor(file: File) : this(Input.FileInput(file))

  /** Creates a reader backed by the plugin's `config.yml`. */
  constructor(plugin: Plugin) : this(
    Input.FileInput(File(plugin.dataFolder, "config.yml")) {
      val file = File(plugin.dataFolder, "config.yml")
      if (!file.exists()) plugin.saveResource("config.yml", false)
    }
  )

  /** Creates a read-only reader from highest-to-lowest-priority [sections]. */
  constructor(vararg sections: ConfigurationSection?) : this(
    Input.SectionsInput(sections.filterNotNull())
  )

  /**
   * Controls whether a file-backed reader saves defaults and normalized values.
   * This option has no effect on section-backed readers, which never mutate sources.
   */
  fun writeDefaults(enabled: Boolean): ConfigReader = apply {
    writeDefaults = enabled
  }

  /**
   * Reads from a nested section instead of the source root.
   * Missing sections are created only for file-backed readers when writing is enabled.
   */
  fun section(path: String): ConfigReader = apply {
    require(path.isNotBlank()) { "Configuration section path must not be blank" }
    sectionPath = path
  }

  /**
   * Uses the file root as a lower-priority fallback for the section selected by [section].
   *
   * Nested sections are merged recursively. Values in the selected section always
   * have priority over values at the file root.
   *
   * This option is available only for file-backed readers.
   */
  fun fallbackRoot(): ConfigReader = apply {
    require(input is Input.FileInput) {
      "Root fallback requires a file-backed ConfigReader"
    }
    useRootFallback = true
  }

  /**
   * Fills missing values below [targetPath] from [sourcePath].
   *
   * [targetPath] is relative to the section selected by [section], while
   * [sourcePath] is absolute from the file root. Values in the selected section
   * always have priority over fallback values.
   *
   * This overload is available only for file-backed readers.
   */
  fun fallbackSection(targetPath: String, sourcePath: String): ConfigReader = apply {
    require(input is Input.FileInput) {
      "Path-based fallback sections require a file-backed ConfigReader"
    }
    require(targetPath.isNotBlank()) { "Fallback target path must not be blank" }
    require(sourcePath.isNotBlank()) { "Fallback source path must not be blank" }
    fallbackSections += FallbackSection.FilePath(targetPath, sourcePath)
  }

  /**
   * Fills missing values below [targetPath] from [source].
   *
   * Values in the reader's primary source always have priority. A null source
   * is ignored, which allows optional Bukkit sections to be passed directly.
   */
  fun fallbackSection(targetPath: String, source: ConfigurationSection?): ConfigReader = apply {
    require(targetPath.isNotBlank()) { "Fallback target path must not be blank" }
    if (source != null) {
      fallbackSections += FallbackSection.Section(targetPath, source)
    }
  }

  /** Reads the configured source as [T]. */
  inline fun <reified T : Any> read(): T = read(T::class)

  /** Reads the configured source as [clazz]. */
  fun <T : Any> read(clazz: KClass<T>): T = when (val current = input) {
    is Input.FileInput -> readFile(current, clazz)
    is Input.SectionsInput -> readSections(current.sections, clazz)
  }

  private fun <T : Any> readFile(input: Input.FileInput, clazz: KClass<T>): T {
    input.prepare?.invoke()
    val yaml = YamlConfiguration.loadConfiguration(input.file)
    val target = sectionPath?.let { path ->
      yaml.getConfigurationSection(path)
        ?: if (writeDefaults) yaml.createSection(path) else YamlConfiguration()
    } ?: yaml

    val effective = mergeFallbackSections(target, yaml) { fallback ->
      when (fallback) {
        is FallbackSection.FilePath -> yaml.getConfigurationSection(fallback.sourcePath)
        is FallbackSection.Section -> fallback.source
      }
    }
    val instance = ClassMapper.create(clazz, effective)
    if (writeDefaults) {
      if (effective !== target) ClassMapper.write(instance, target)
      yaml.save(input.file)
    }
    return instance
  }

  private fun <T : Any> readSections(
    sections: List<ConfigurationSection>,
    clazz: KClass<T>
  ): T {
    val selected = sectionPath?.let { path ->
      sections.mapNotNull { it.getConfigurationSection(path) }
    } ?: sections
    val merged = YamlConfiguration()
    fallbackSections.asReversed().forEach { fallback ->
      val source = when (fallback) {
        is FallbackSection.Section -> fallback.source
        is FallbackSection.FilePath -> error(
          "Path-based fallback sections require a file-backed ConfigReader"
        )
      }
      overlayAt(merged, fallback.targetPath, source)
    }
    selected.asReversed().forEach { source -> overlay(merged, source) }
    return ClassMapper.create(clazz, merged)
  }

  private fun mergeFallbackSections(
    primary: ConfigurationSection,
    root: ConfigurationSection,
    source: (FallbackSection) -> ConfigurationSection?
  ): ConfigurationSection {
    if (!useRootFallback && fallbackSections.isEmpty()) return primary

    return YamlConfiguration().also { merged ->
      if (useRootFallback) overlay(merged, root)
      fallbackSections.asReversed().forEach { fallback ->
        source(fallback)?.let { overlayAt(merged, fallback.targetPath, it) }
      }
      overlay(merged, primary)
    }
  }

  private fun overlayAt(
    target: ConfigurationSection,
    path: String,
    source: ConfigurationSection
  ) {
    val destination = target.getConfigurationSection(path) ?: target.createSection(path)
    overlay(destination, source)
  }

  private fun overlay(target: ConfigurationSection, source: ConfigurationSection) {
    source.getKeys(false).forEach { key ->
      val sourceSection = source.getConfigurationSection(key)
      if (sourceSection != null) {
        val targetSection = target.getConfigurationSection(key) ?: run {
          target.set(key, null)
          target.createSection(key)
        }
        overlay(targetSection, sourceSection)
      } else {
        target.set(key, source.get(key))
      }
    }
  }

  private sealed interface Input {
    data class FileInput(
      val file: File,
      val prepare: (() -> Unit)? = null
    ) : Input

    data class SectionsInput(
      val sections: List<ConfigurationSection>
    ) : Input
  }

  private sealed interface FallbackSection {
    val targetPath: String

    data class FilePath(
      override val targetPath: String,
      val sourcePath: String
    ) : FallbackSection

    data class Section(
      override val targetPath: String,
      val source: ConfigurationSection
    ) : FallbackSection
  }
}
