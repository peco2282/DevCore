package com.peco2282.devcore.config.serializers

import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

object BukkitSerializers {
  val ITEM_STACK: Serializer<ItemStack> = object : Serializer<ItemStack> {
    override fun deserialize(value: Any?): ItemStack {
      return value as? ItemStack ?: ItemStack.empty()
    }

    override fun serialize(value: ItemStack): Any {
      return value
    }
  }

  val LOCATION: Serializer<Location> = object : Serializer<Location> {
    override fun deserialize(value: Any?): Location {
      return value as? Location ?: throw IllegalArgumentException("Value is not a Location")
    }

    override fun serialize(value: Location): Any {
      return value
    }
  }

  val VECTOR: Serializer<Vector> = object : Serializer<Vector> {
    override fun deserialize(value: Any?): Vector {
      return value as? Vector ?: throw IllegalArgumentException("Value is not a Vector")
    }

    override fun serialize(value: Vector): Any {
      return value
    }
  }

  internal fun registerAll() {
    Serializer.registerer(ItemStack::class, ITEM_STACK)
    Serializer.registerer(Location::class, LOCATION)
    Serializer.registerer(Vector::class, VECTOR)
  }
}
