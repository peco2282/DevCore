package com.peco2282.devcore.packet

import org.bukkit.Bukkit

/**
 * Factory for creating appropriate packets or NMS objects according to the server version.
 */
object AutoVersioner {
  private val version = Bukkit.getServer().javaClass.`package`.name.split(".")[3]

  /**
   * Gets the class according to the version from the class name.
   * Example: "net.minecraft.network.protocol.game.ClientboundChatPacket"
   */
  fun getNMSClass(className: String): Class<*> {
    return Class.forName(className)
  }

  /**
   * Creates an instance of the specified class.
   */
  fun <T> create(className: String, vararg args: Any?): T {
    val clazz = getNMSClass(className)
    val constructor = clazz.constructors.find { it.parameterCount == args.size }
      ?: throw NoSuchMethodException("Constructor for $className with ${args.size} parameters not found")
    @Suppress("UNCHECKED_CAST")
    return constructor.newInstance(*args) as T
  }
}
