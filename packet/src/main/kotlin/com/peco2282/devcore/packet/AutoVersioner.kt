package com.peco2282.devcore.packet

import org.bukkit.Bukkit

/**
 * サーバーのバージョンに応じて適切なパケットや NMS オブジェクトを生成するためのファクトリ。
 */
object AutoVersioner {
  private val version = Bukkit.getServer().javaClass.`package`.name.split(".")[3]

  /**
   * クラス名からバージョンに応じたクラスを取得します。
   * 例: "net.minecraft.network.protocol.game.ClientboundChatPacket"
   */
  fun getNMSClass(className: String): Class<*> {
    return Class.forName(className)
  }

  /**
   * 指定したクラスのインスタンスを生成します。
   */
  fun <T> create(className: String, vararg args: Any?): T {
    val clazz = getNMSClass(className)
    val constructor = clazz.constructors.find { it.parameterCount == args.size }
      ?: throw NoSuchMethodException("Constructor for $className with ${args.size} parameters not found")
    @Suppress("UNCHECKED_CAST")
    return constructor.newInstance(*args) as T
  }
}
