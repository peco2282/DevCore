package com.peco2282.devcore.packet

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

object PacketAPI {
  private var internal: PacketHub? = null

  private fun className(version: String) =
    "com.peco2282.devcore.packet.v${version.replace(".", "_")}.PacketHubImpl"

  fun init(plugin: Plugin) {
    val version = Bukkit.getMinecraftVersion()
    val className = when (version) {
      in "1.20.4"..<"1.20.6" -> className("1.20.4")
      in "1.20.6"..<"1.21.4" -> className("1.20.6")
      in "1.21.4"..<"1.21.11" -> className("1.21.4")
      in "1.21.11"..<"1.22" -> className("1.21.11")
      // 他のバージョンも同様に追加
      else -> {
        plugin.logger.warning("Unsupported version for Packet NMS: $version")
        null
      }
    }

    internal = className?.let {
      try {
        Class.forName(it).getDeclaredConstructor().newInstance() as? PacketHub
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
    return internal?.getNetworkSettings(player) ?: Packets.NetworkSettingsImpl()
  }

  fun sendRawPacket(player: Player, channel: String, buf: ByteBuf) {
    internal?.sendRawPacket(player, channel, buf)
  }

  fun getCoroutineDispatcher(player: Player): CoroutineDispatcher? {
    return internal?.getCoroutineDispatcher(player)
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

  fun sendParticles(
    player: Player,
    type: Particle,
    location: Location,
    amount: Int,
    offset: Vector,
    extra: Double,
    data: Any?
  ) {
    internal?.sendParticles(player, type, location, amount, offset, extra, data)
  }

  fun sendFakeBlocks(player: Player, builder: FakeBlockBuilder.() -> Unit) {
    internal?.sendFakeBlocks(player, builder)
  }
}