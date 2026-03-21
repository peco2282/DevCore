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

object PacketAPI: PacketHub {
  private var delegate: PacketHub? = null

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

    delegate = className?.let {
      try {
        Class.forName(it).getDeclaredConstructor().newInstance() as? PacketHub
      } catch (e: Exception) {
        plugin.logger.warning("Failed to load Packet NMS for $version: ${e.message}")
        null
      }
    }

    if (delegate != null) {
      plugin.logger.info("Packet NMS initialized for version: $version")
    }
  }

  override fun injectPlayer(player: Player) = delegate?.injectPlayer(player) ?: throw UnsupportedOperationException("Player injection is not supported on this version")
  override fun removePlayer(player: Player) = delegate?.removePlayer(player) ?: throw UnsupportedOperationException("Player removal is not supported on this version")
  override fun sendPacket(player: Player, packet: Any) = delegate?.sendPacket(player, packet) ?: throw UnsupportedOperationException("Packet sending is not supported on this version")

  fun createPacketListener(): PacketListener {
    return Packets.createPacketListener()
  }

  override fun createFakeEntityBuilder(player: Player, type: EntityType, location: Location): FakeEntityBuilder {
    return delegate?.createFakeEntityBuilder(player, type, location)
      ?: throw UnsupportedOperationException("FakeEntityBuilder is not supported on this version")
  }

  override fun getNetworkSettings(player: Player): NetworkSettings {
    return delegate?.getNetworkSettings(player) ?: Packets.NetworkSettingsImpl()
  }

  override fun sendRawPacket(player: Player, channel: String, buf: ByteBuf) {
    delegate?.sendRawPacket(player, channel, buf)
  }

  override fun getCoroutineDispatcher(player: Player): CoroutineDispatcher? {
    return delegate?.getCoroutineDispatcher(player)
  }

  override fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
    delegate?.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut)
  }

  override fun sendActionBar(player: Player, message: String) {
    delegate?.sendActionBar(player, message)
  }

  override fun sendSound(
    player: Player,
    type: Sound,
    volume: Float,
    pitch: Float,
    relative: Boolean,
    offset: Vector
  ) {
    delegate?.sendSound(player, type, volume, pitch, relative, offset)
  }

  override fun sendParticles(
    player: Player,
    type: Particle,
    location: Location,
    amount: Int,
    offset: Vector,
    extra: Double,
    data: Any?
  ) {
    delegate?.sendParticles(player, type, location, amount, offset, extra, data)
  }

  override fun sendFakeBlocks(player: Player, builder: FakeBlockBuilder.() -> Unit) {
    delegate?.sendFakeBlocks(player, builder)
  }
}