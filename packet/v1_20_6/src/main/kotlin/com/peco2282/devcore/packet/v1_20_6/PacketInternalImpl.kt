package com.peco2282.devcore.packet.v1_20_6

import com.peco2282.devcore.packet.*
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class PacketInternalImpl : PacketInternal {
  private val HANDLER_NAME = "devcore_packet_handler"

  override fun injectPlayer(player: Player) {
    val craftPlayer = player as CraftPlayer
    val connection = try {
      craftPlayer.handle.connection
    } catch (e: Exception) {
      return
    }
    if (connection == null) return

    val channel = connection.connection.channel
    if (channel.pipeline().get(HANDLER_NAME) != null) return

    channel.pipeline().addBefore("packet_handler", HANDLER_NAME, Packets.PacketHandler(player))
  }

  override fun removePlayer(player: Player) {
    val craftPlayer = player as CraftPlayer
    val channel = craftPlayer.handle.connection.connection.channel
    channel.eventLoop().submit {
      if (channel.pipeline().get(HANDLER_NAME) != null) {
        channel.pipeline().remove(HANDLER_NAME)
      }
    }
  }

  override fun sendPacket(player: Player, packet: Any) {
    if (packet is Packet<*>) {
      (player as CraftPlayer).handle.connection.send(packet)
    }
  }

  override fun getNetworkSettings(player: Player): NetworkSettings {
    val craftPlayer = player as CraftPlayer
    val channel = craftPlayer.handle.connection.connection.channel
    val handler = channel.pipeline().get(HANDLER_NAME) as? Packets.PacketHandler
    return handler?.settings ?: Packets.NetworkSettingsImpl()
  }

  override fun createFakeEntityBuilder(player: Player, type: EntityType, location: Location): FakeEntityBuilder {
    throw UnsupportedOperationException("FakeEntityBuilder is not yet implemented for v1_20_6")
  }

  override fun sendRawPacket(player: Player, channel: String, buf: FriendlyByteBuf) {
    throw UnsupportedOperationException("sendRawPacket is not yet implemented for v1_20_6")
  }

  override fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
    throw UnsupportedOperationException("sendTitle is not yet implemented for v1_20_6")
  }

  override fun sendActionBar(player: Player, message: String) {
    throw UnsupportedOperationException("sendActionBar is not yet implemented for v1_20_6")
  }

  override fun sendSound(
    player: Player,
    type: org.bukkit.Sound,
    volume: Float,
    pitch: Float,
    relative: Boolean,
    offset: org.bukkit.util.Vector
  ) {
    throw UnsupportedOperationException("sendSound is not yet implemented for v1_20_6")
  }
}
