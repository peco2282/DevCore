package com.peco2282.devcore.entity

import org.bukkit.entity.Entity
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.java.JavaPlugin


/**
 * Sets metadata on this entity with the specified key and value.
 *
 * This is a convenience extension function that wraps the value in a [FixedMetadataValue]
 * and associates it with the entity using the provided plugin instance.
 *
 * @param plugin The plugin instance that owns this metadata
 * @param key The metadata key to set
 * @param value The value to associate with the key
 */
fun Entity.setMetadata(plugin: JavaPlugin, key: String, value: Any) {
  this.setMetadata(key, FixedMetadataValue(plugin, value))
}

/**
 * Retrieves metadata value from this entity for the specified key.
 *
 * This is a convenience extension function that retrieves the first metadata value
 * associated with the given key and attempts to cast it to the specified type.
 *
 * @param T The expected type of the metadata value
 * @param key The metadata key to retrieve
 * @return The metadata value cast to type [T], or null if no metadata exists for the key
 *         or if the value cannot be cast to the specified type
 */
inline fun <reified T> Entity.getMetadataValue(key: String): T? {
  return this.getMetadata(key).firstOrNull()?.value() as? T
}
