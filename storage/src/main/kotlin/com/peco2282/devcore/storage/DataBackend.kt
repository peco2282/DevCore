package com.peco2282.devcore.storage

import com.peco2282.devcore.config.reflection.ClassMapper
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Where player data lives. A backend only moves text, which keeps it small and makes it easy to
 * back by a database or Redis instead of files.
 *
 * Threading contract: [encode] runs on the thread that asks for a save (normally the main thread),
 * so the snapshot is consistent. [read], [write] and [delete] block and run off the main thread.
 */
interface DataBackend<V : Any> {
  /** Serializes [value]. Must not touch anything another thread may be changing afterwards. */
  fun encode(value: V): String

  /** Parses text produced by [encode]. @throws Exception if the text is corrupt. */
  fun decode(text: String): V

  /** The stored text for [id], or `null` if nothing is stored. */
  fun read(id: UUID): String?

  /** Stores [text] for [id], replacing any earlier value. A crash mid-write must not lose the old value. */
  fun write(id: UUID, text: String)

  /** Removes the stored value for [id], if any. */
  fun delete(id: UUID)

  /** Called when [read] returned text that [decode] could not parse, so a backend can set it aside. */
  fun quarantine(id: UUID) {}
}

/**
 * One YAML file per player, `<dir>/<uuid>.yml`, mapped to a data class with the `config` module.
 *
 * Writes go to a temporary file first and are then moved over the old one, so a crash cannot leave
 * a half-written file. Fields missing from an older file take their default values.
 */
class YamlFileBackend<V : Any>(private val dir: File, private val type: KClass<V>) : DataBackend<V> {
  init {
    dir.mkdirs()
  }

  private fun file(id: UUID) = File(dir, "$id.yml")

  override fun encode(value: V): String = YamlConfiguration().also { ClassMapper.write(value, it) }.saveToString()

  override fun decode(text: String): V {
    val yaml = YamlConfiguration()
    yaml.loadFromString(text)
    return ClassMapper.create(type, yaml)
  }

  override fun read(id: UUID): String? = file(id).takeIf { it.isFile }?.readText()

  override fun write(id: UUID, text: String) {
    val target = file(id).toPath()
    val temp = File(dir, "$id.yml.tmp").toPath()
    Files.writeString(temp, text)
    try {
      Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    } catch (_: AtomicMoveNotSupportedException) {
      Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
    }
  }

  override fun delete(id: UUID) {
    Files.deleteIfExists(file(id).toPath())
  }

  override fun quarantine(id: UUID) {
    val source = file(id)
    if (source.isFile) source.renameTo(File(dir, "$id.yml.corrupt-${System.currentTimeMillis()}"))
  }
}

/** A [YamlFileBackend] for [V] in [dir]. */
inline fun <reified V : Any> yamlBackend(dir: File): DataBackend<V> = YamlFileBackend(dir, V::class)
