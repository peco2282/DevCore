package com.peco2282.devcore.packet

import net.minecraft.network.FriendlyByteBuf
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

interface PacketInternal {
  fun injectPlayer(player: Player)
  fun removePlayer(player: Player)
  fun sendPacket(player: Player, packet: Any)
  fun getNetworkSettings(player: Player): NetworkSettings
  fun createFakeEntityBuilder(player: Player, type: EntityType, location: Location): FakeEntityBuilder
  fun sendRawPacket(player: Player, channel: String, buf: FriendlyByteBuf)
  fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)
  fun sendActionBar(player: Player, message: String)
  fun sendSound(
    player: Player,
    type: Sound,
    volume: Float,
    pitch: Float,
    relative: Boolean,
    offset: Vector
  )
}

internal object InternalAPI {
  private var internal: PacketInternal? = null

  fun init(plugin: Plugin) {
    val version = Bukkit.getMinecraftVersion()
    val className = when (version) {
      "1.20.4" -> "com.peco2282.devcore.packet.v1_20_4.PacketInternalImpl"
      "1.20.6" -> "com.peco2282.devcore.packet.v1_20_6.PacketInternalImpl"
      "1.21.4" -> "com.peco2282.devcore.packet.v1_21_4.PacketInternalImpl"
      "1.21.11" -> "com.peco2282.devcore.packet.v1_21_11.PacketInternalImpl"
      // 他のバージョンも同様に追加
      else -> {
        plugin.logger.warning("Unsupported version for Packet NMS: $version")
        null
      }
    }

    internal = className?.let {
      try {
        Class.forName(it).getDeclaredConstructor().newInstance() as? PacketInternal
      } catch (e: Exception) {
        plugin.logger.warning("Failed to load Packet NMS for $version: ${e.message}")
        null
      }
    }

    if (internal != null) {
      plugin.logger.info("Packet NMS initialized for version: $version")
    }
  }

  fun injectPlayer(player: Player) = internal?.injectPlayer(player)
  fun removePlayer(player: Player) = internal?.removePlayer(player)
  fun sendPacket(player: Player, packet: Any) = internal?.sendPacket(player, packet)

  fun createPacketListener(): PacketListener {
    return Packets.createPacketListener()
  }

  fun createFakeEntityBuilder(player: Player, type: EntityType, location: Location): FakeEntityBuilder {
    return internal?.createFakeEntityBuilder(player, type, location)
      ?: throw UnsupportedOperationException("FakeEntityBuilder is not supported on this version")
  }

  fun getNetworkSettings(player: Player): NetworkSettings {
    return internal?.getNetworkSettings(player) ?: Packets.getNetworkSettings(player)
  }

  fun sendRawPacket(player: Player, channel: String, buf: FriendlyByteBuf) {
    internal?.sendRawPacket(player, channel, buf)
  }

  fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
    internal?.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut)
  }

  fun sendActionBar(player: Player, message: String) {
    internal?.sendActionBar(player, message)
  }

  fun sendSound(
    player: Player,
    type: Sound,
    volume: Float,
    pitch: Float,
    relative: Boolean,
    offset: Vector
  ) {
    internal?.sendSound(player, type, volume, pitch, relative, offset)
  }
}