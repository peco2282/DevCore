package com.peco2282.devcore.packet

import org.bukkit.Bukkit

/**
 * Utility object for dynamically loading NMS classes and creating instances at runtime.
 *
 * Used to abstract version-specific class loading without direct compile-time dependencies.
 */
object AutoVersioner {
  private val version = Bukkit.getServer().javaClass.`package`.name.split(".")[3]

  /**
   * Loads and returns the [Class] for the given fully-qualified [className].
   *
   * Example: `"net.minecraft.network.protocol.game.ClientboundChatPacket"`
   *
   * @param className The fully-qualified class name to load.
   * @throws ClassNotFoundException if the class cannot be found on the classpath.
   */
  fun getNMSClass(className: String): Class<*> {
    return Class.forName(className)
  }

  /**
   * Creates and returns an instance of the class identified by [className],
   * using the constructor that matches the number of provided [args].
   *
   * @param T The expected return type.
   * @param className The fully-qualified class name to instantiate.
   * @param args Constructor arguments.
   * @throws NoSuchMethodException if no constructor with the given parameter count is found.
   */
  fun <T> create(className: String, vararg args: Any?): T {
    val clazz = getNMSClass(className)
    val constructor = clazz.constructors.find { it.parameterCount == args.size }
      ?: throw NoSuchMethodException("Constructor for $className with ${args.size} parameters not found")
    @Suppress("UNCHECKED_CAST")
    return constructor.newInstance(*args) as T
  }
}
