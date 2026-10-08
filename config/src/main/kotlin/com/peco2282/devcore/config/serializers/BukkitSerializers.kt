package com.peco2282.devcore.config.serializers

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.UUID

object BukkitSerializers {
  val ITEM_STACK: Serializer<ItemStack> = object : Serializer<ItemStack> {
    override fun deserialize(value: Any?): ItemStack {
      if (value == null) return ItemStack.empty()
      if (value is ItemStack) return value.clone()

      val values = value.asConfigurationMap("ItemStack")
        .filterValues { it != null }
        .mapValues { (_, entry) -> entry!! }
      return ItemStack.deserialize(values)
    }

    override fun serialize(value: ItemStack): Any {
      return value.serialize()
    }
  }

  val LOCATION: Serializer<Location> = object : Serializer<Location> {
    override fun deserialize(value: Any?): Location {
      if (value is Location) return value.clone()

      val values = value.asConfigurationMap("Location")
      val world = values["world"].toWorld()
      return Location(
        world,
        values.requiredDouble("x", "Location"),
        values.requiredDouble("y", "Location"),
        values.requiredDouble("z", "Location"),
        values.optionalFloat("yaw"),
        values.optionalFloat("pitch")
      )
    }

    override fun serialize(value: Location): Any {
      return buildMap<String, Any> {
        value.world?.let { put("world", it.name) }
        put("x", value.x)
        put("y", value.y)
        put("z", value.z)
        put("yaw", value.yaw)
        put("pitch", value.pitch)
      }
    }
  }

  val VECTOR: Serializer<Vector> = object : Serializer<Vector> {
    override fun deserialize(value: Any?): Vector {
      if (value is Vector) return value.clone()

      val values = value.asConfigurationMap("Vector")
      return Vector(
        values.requiredDouble("x", "Vector"),
        values.requiredDouble("y", "Vector"),
        values.requiredDouble("z", "Vector")
      )
    }

    override fun serialize(value: Vector): Any {
      return mapOf(
        "x" to value.x,
        "y" to value.y,
        "z" to value.z
      )
    }
  }

  internal fun registerAll() {
    Serializer.registerer(ItemStack::class, ITEM_STACK)
    Serializer.registerer(Location::class, LOCATION)
    Serializer.registerer(Vector::class, VECTOR)
  }

  private fun Any?.asConfigurationMap(typeName: String): Map<String, Any?> = when (this) {
    is ConfigurationSection -> getKeys(false).associateWith { key ->
      getConfigurationSection(key)?.toConfigurationMap() ?: get(key)
    }
    is Map<*, *> -> entries.associate { (key, value) ->
      require(key is String) { "$typeName keys must be strings" }
      key to value.toConfigurationValue()
    }
    else -> throw IllegalArgumentException("Value is not a $typeName or configuration map")
  }

  private fun ConfigurationSection.toConfigurationMap(): Map<String, Any?> =
    getKeys(false).associateWith { key ->
      getConfigurationSection(key)?.toConfigurationMap() ?: get(key)
    }

  private fun Any?.toConfigurationValue(): Any? = when (this) {
    is ConfigurationSection -> toConfigurationMap()
    is Map<*, *> -> entries.associate { (key, value) ->
      require(key is String) { "Configuration map keys must be strings" }
      key to value.toConfigurationValue()
    }
    is List<*> -> map { it.toConfigurationValue() }
    else -> this
  }

  private fun Map<String, Any?>.requiredDouble(key: String, typeName: String): Double {
    val value = this[key]
    require(value is Number) { "$typeName.$key must be a number" }
    return value.toDouble()
  }

  private fun Map<String, Any?>.optionalFloat(key: String): Float {
    val value = this[key] ?: return 0f
    require(value is Number) { "Location.$key must be a number" }
    return value.toFloat()
  }

  private fun Any?.toWorld(): World? = when (this) {
    null -> null
    is World -> this
    is UUID -> Bukkit.getWorld(this)
      ?: throw IllegalArgumentException("World with UUID $this does not exist")
    is String -> Bukkit.getWorld(this)
      ?: runCatching { UUID.fromString(this) }.getOrNull()?.let { uuid -> Bukkit.getWorld(uuid) }
      ?: throw IllegalArgumentException("World '$this' does not exist")
    else -> throw IllegalArgumentException("Location.world must be a world name or UUID")
  }
}
