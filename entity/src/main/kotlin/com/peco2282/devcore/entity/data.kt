package com.peco2282.devcore.entity

import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import kotlin.reflect.KClass

/**
 * A DSL interface for reading and writing persistent data on an entity's [PersistentDataContainer].
 *
 * Provides operator overloads for a more idiomatic Kotlin experience when working with
 * Bukkit's persistent data API.
 */
interface PersistentDataEditor {
  /** The underlying [PersistentDataContainer] being edited. */
  val origin: PersistentDataContainer

  /**
   * Returns true if the container has a value associated with the given [key].
   *
   * @param key the key to check
   */
  fun has(key: NamespacedKey): Boolean

  /**
   * Operator alias for [has].
   *
   * @param key the key to check
   */
  operator fun contains(key: NamespacedKey): Boolean = has(key)

  /**
   * Sets a value in the container with an explicit [PersistentDataType].
   *
   * @param key the key to store the value under
   * @param data the data type descriptor
   * @param value the value to store
   */
  operator fun <T : Any> set(key: NamespacedKey, data: PersistentDataType<T, T>, value: T)

  /**
   * Removes the value associated with the given [key] from the container.
   *
   * @param key the key to remove
   */
  fun remove(key: NamespacedKey)
}

/**
 * Returns the [PersistentDataType] corresponding to the given Kotlin [KClass].
 *
 * Supports the following types: [String], [Int], [Double], [Float], [Long], [Byte], [Short],
 * [Boolean], [ByteArray], [IntArray], and [LongArray].
 *
 * @param clazz the Kotlin class to look up
 * @return the matching [PersistentDataType]
 * @throws IllegalArgumentException if the type is not supported
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> getDataType(clazz: KClass<T>): PersistentDataType<T, T> = when (clazz) {
  String::class -> PersistentDataType.STRING
  Int::class -> PersistentDataType.INTEGER
  Double::class -> PersistentDataType.DOUBLE
  Float::class -> PersistentDataType.FLOAT
  Long::class -> PersistentDataType.LONG
  Byte::class -> PersistentDataType.BYTE
  Short::class -> PersistentDataType.SHORT
  Boolean::class -> PersistentDataType.BOOLEAN
  ByteArray::class -> PersistentDataType.BYTE_ARRAY
  IntArray::class -> PersistentDataType.INTEGER_ARRAY
  LongArray::class -> PersistentDataType.LONG_ARRAY
  else -> throw IllegalArgumentException("Unsupported type: ${clazz.simpleName}")
} as PersistentDataType<T, T>

/**
 * Retrieves a value from the container using the inferred type [T].
 *
 * @param key the key to look up
 * @return the stored value, or null if not present
 */
inline operator fun <reified T : Any> PersistentDataEditor.get(key: NamespacedKey): T? {
  val type: PersistentDataType<T, T> = getDataType(T::class)
  return origin.get(key, type)
}

/**
 * Stores a value in the container using the inferred type [T].
 *
 * @param key the key to store the value under
 * @param value the value to store
 */
inline operator fun <reified T : Any> PersistentDataEditor.set(key: NamespacedKey, value: T) {
  val type: PersistentDataType<T, T> = getDataType(T::class)
  this[key, type] = value
}

/**
 * Internal implementation of [PersistentDataEditor] backed by a [PersistentDataContainer].
 *
 * @param origin the container to delegate all operations to
 */
internal class PersistentDataEditorImpl(override val origin: PersistentDataContainer) : PersistentDataEditor {
  override fun has(key: NamespacedKey): Boolean = origin.has(key)
  override fun <T : Any> set(key: NamespacedKey, data: PersistentDataType<T, T>, value: T) =
    origin.set(key, data, value)
  override fun remove(key: NamespacedKey) = origin.remove(key)
}

/**
 * Edits the persistent data of this entity using a DSL builder.
 *
 * Example usage:
 * ```
 * entity.data {
 *   this[NamespacedKey(plugin, "my_key")] = "hello"
 *   remove(NamespacedKey(plugin, "old_key"))
 * }
 * ```
 *
 * @param editor a lambda with receiver that reads or writes persistent data
 */
fun Entity.data(editor: PersistentDataEditor.() -> Unit) {
  PersistentDataEditorImpl(this.persistentDataContainer).apply(editor)
}
