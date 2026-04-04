package com.peco2282.devcore.packet

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicInteger


private val counter = AtomicInteger(0x00)

/**
 * Represents a typed packet descriptor with a unique auto-incremented [id].
 *
 * @param T The Bukkit type associated with this packet kind (e.g. [Player], [Entity]).
 * @property id The unique integer identifier for this packet type.
 * @property packetClass The [Class] of the associated type [T].
 */
interface PacketType<T> {
  val id: Int
  val packetClass: Class<T>

  companion object {
    /**
     * Creates a new [PacketType] instance for the reified type [T] with an auto-incremented ID.
     */
    internal inline operator fun <reified T> invoke(): PacketType<T> {
      return object : PacketType<T> {
        override val id: Int = counter.getAndIncrement()
        override val packetClass: Class<T>
          get() = T::class.java

        override fun equals(other: Any?): Boolean = other is PacketType<*> && other.id == id
        override fun hashCode(): Int = id
        override fun toString(): String = "PacketType(id=$id, packetClass=${packetClass.simpleName.uppercase()})"
      }
    }
  }
}


/**
 * Predefined [PacketType] constants for common Bukkit types.
 */
object PacketTypes {
  /** Packet type associated with a [Player]. */
  val PLAYER = PacketType<Player>()

  /** Packet type associated with an [Entity]. */
  val ENTITY = PacketType<Entity>()

  /** Packet type associated with a [Block]. */
  val BLOCK = PacketType<Block>()
}
