package com.peco2282.devcore.config.migration

import com.peco2282.devcore.config.ConfigReader
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/** Marks the integer property that records the schema version. */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigVersion

/** Migration configuration for one config load. */
class ConfigLoadBuilder<T : Any> internal constructor(private val type: KClass<T>) {
  private var migrations: MigrationPlan? = null

  fun migrations(block: MigrationsBuilder.() -> Unit) {
    check(migrations == null) { "Migrations may only be configured once" }
    migrations = MigrationsBuilder().apply(block).build()
  }

  internal fun load(file: File): T {
    migrations?.let { migrate(file, type, it) }
    return ConfigReader(file).read(type)
  }
}

/** Defines ordered `version -> version + 1` migrations. */
class MigrationsBuilder {
  private val steps = linkedMapOf<Int, MigrationStep.() -> Unit>()

  fun from(version: Int, block: MigrationStep.() -> Unit) {
    require(version >= 0) { "Migration versions must be non-negative" }
    check(steps.put(version, block) == null) { "Migration from version $version is already defined" }
  }

  internal fun build(): MigrationPlan = MigrationPlan(steps.toMap())
}

/** Mutable operations available inside a migration. */
class MigrationStep internal constructor(private val yaml: YamlConfiguration) {
  fun rename(from: String, to: String) {
    require(from.isNotBlank() && to.isNotBlank()) { "Migration paths must not be blank" }
    if (!yaml.contains(from)) return
    require(!yaml.contains(to)) { "Cannot rename '$from' to existing path '$to'" }
    yaml.set(to, yaml.get(from))
    yaml.set(from, null)
  }

  fun remove(path: String) {
    require(path.isNotBlank()) { "Migration path must not be blank" }
    yaml.set(path, null)
  }

  fun transform(path: String, transform: (Any?) -> Any?) {
    require(path.isNotBlank()) { "Migration path must not be blank" }
    require(yaml.contains(path)) { "Cannot transform missing path '$path'" }
    yaml.set(path, transform(yaml.get(path)))
  }
}

internal data class MigrationPlan(val steps: Map<Int, MigrationStep.() -> Unit>)

/** Loads [type] after applying its configured migrations. */
fun <T : Any> loadMigrated(file: File, type: KClass<T>, block: ConfigLoadBuilder<T>.() -> Unit): T =
  ConfigLoadBuilder(type).apply(block).load(file)

private fun <T : Any> migrate(file: File, type: KClass<T>, plan: MigrationPlan) {
  if (!file.exists()) return
  val (versionKey, targetVersion) = versionMetadata(type)
  val yaml = YamlConfiguration.loadConfiguration(file)
  val currentVersion = yaml.getInt(versionKey, targetVersion)
  require(currentVersion <= targetVersion) {
    "Config version $currentVersion is newer than supported version $targetVersion"
  }
  if (currentVersion == targetVersion) return

  val backup = file.resolveSibling("${file.name}.bak-v$currentVersion")
  Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING)
  try {
    var version = currentVersion
    while (version < targetVersion) {
      val migration = requireNotNull(plan.steps[version]) { "Missing migration from version $version" }
      MigrationStep(yaml).migration()
      version++
      yaml.set(versionKey, version)
    }
    yaml.save(file)
  } catch (failure: Throwable) {
    Files.copy(backup.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
    throw failure
  }
}

private fun <T : Any> versionMetadata(type: KClass<T>): Pair<String, Int> {
  val constructor = requireNotNull(type.primaryConstructor) { "Migrated config must have a primary constructor" }
  val parameter = constructor.parameters.singleOrNull { it.findAnnotation<ConfigVersion>() != null }
    ?: error("Exactly one primary-constructor property must be annotated with @ConfigVersion")
  require(constructor.parameters.all { it.isOptional }) {
    "Migrated config constructor parameters must have defaults so the target version can be read"
  }
  val name = requireNotNull(parameter.name)
  val defaults = constructor.callBy(emptyMap())
  val property = type.memberProperties.single { it.name == name }
  val version = property.getter.call(defaults) as? Int
    ?: error("@ConfigVersion property must be an Int")
  return name to version
}

private fun File.resolveSibling(name: String): File = File(absoluteFile.parentFile, name)
