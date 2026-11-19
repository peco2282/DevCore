package com.peco2282.devcore.pdc

import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import kotlin.reflect.typeOf

/**
 * A typed key for a [PersistentDataContainer]: the [NamespacedKey] and the value type together,
 * so reads and writes cannot disagree about the type.
 *
 * ```kotlin
 * val KILLS = pdcKey<Int>(plugin, "kills")
 * player.persistentDataContainer[KILLS] = 3
 * val kills: Int? = player.persistentDataContainer[KILLS]
 * ```
 */
class PdcKey<T : Any>(val key: NamespacedKey, val type: PersistentDataType<*, T>) {
  override fun toString(): String = "PdcKey($key)"
  override fun equals(other: Any?): Boolean = other is PdcKey<*> && other.key == key
  override fun hashCode(): Int = key.hashCode()
}

/**
 * Creates a [PdcKey] named [name] in [plugin]'s namespace.
 *
 * @param name Lowercase letters, digits and `. _ - /` only (a Bukkit [NamespacedKey] rule).
 * @throws IllegalArgumentException for an invalid name or an unsupported type.
 */
inline fun <reified T : Any> pdcKey(plugin: Plugin, name: String): PdcKey<T> =
  PdcKey(NamespacedKey(plugin, name), PdcTypes.resolve(typeOf<T>()))

/** Creates a [PdcKey] for an existing [NamespacedKey]. */
inline fun <reified T : Any> pdcKey(key: NamespacedKey): PdcKey<T> =
  PdcKey(key, PdcTypes.resolve(typeOf<T>()))

/** Reads the value, or `null` if absent. */
operator fun <T : Any> PersistentDataContainer.get(key: PdcKey<T>): T? = get(key.key, key.type)

/** Writes the value. */
operator fun <T : Any> PersistentDataContainer.set(key: PdcKey<T>, value: T) = set(key.key, key.type, value)

/** Reads the value, or [default] if absent. */
fun <T : Any> PersistentDataContainer.getOrDefault(key: PdcKey<T>, default: T): T = get(key) ?: default

/** Whether a value of the key's type is stored. */
operator fun PersistentDataContainer.contains(key: PdcKey<*>): Boolean = has(key.key, key.type)

/** Removes the value. */
fun PersistentDataContainer.remove(key: PdcKey<*>) = remove(key.key)

/** Reads the value; if absent, stores and returns the result of [create]. */
inline fun <T : Any> PersistentDataContainer.getOrPut(key: PdcKey<T>, create: () -> T): T =
  get(key) ?: create().also { set(key, it) }

/**
 * Replaces the value with `transform(current ?: default)` and returns the new value.
 *
 * ```kotlin
 * container.update(KILLS, default = 0) { it + 1 }
 * ```
 */
inline fun <T : Any> PersistentDataContainer.update(key: PdcKey<T>, default: T, transform: (T) -> T): T =
  transform(get(key) ?: default).also { set(key, it) }
