package com.peco2282.devcore.packet

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.concurrent.atomic.AtomicInteger


private val counter = AtomicInteger(0x00)

interface PacketType<T> {
  val id: Int
  val packetClass: Class<T>

  companion object {
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


object PacketTypes {
  val PLAYER = PacketType<Player>()
  val ENTITY = PacketType<Entity>()
  val BLOCK = PacketType<Block>()
}
