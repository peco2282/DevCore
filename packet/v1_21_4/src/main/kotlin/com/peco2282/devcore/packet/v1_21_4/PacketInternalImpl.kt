package com.peco2282.devcore.packet.v1_21_4

import com.peco2282.devcore.packet.FakeEntityBuilder
import com.peco2282.devcore.packet.NetworkSettings
import com.peco2282.devcore.packet.PacketInternal
import com.peco2282.devcore.packet.Packets
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.game.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundSource
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftNamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*

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
    // TODO: Implement v1_21_4 specific FakeEntityBuilder
    throw UnsupportedOperationException("FakeEntityBuilder is not yet implemented for v1_21_4")
  }

  override fun sendRawPacket(player: Player, channel: String, buf: FriendlyByteBuf) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundCustomPayloadPacket { CustomPacketPayload.Type(ResourceLocation.parse(channel)) }
    )
  }

  override fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
    val connection = (player as CraftPlayer).handle.connection
    connection.send(ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut))
    if (title.isNotEmpty()) {
      connection.send(ClientboundSetTitleTextPacket(PaperAdventure.asVanilla(Component.text(title)) as net.minecraft.network.chat.Component))
    }
    if (subtitle.isNotEmpty()) {
      connection.send(ClientboundSetSubtitleTextPacket(PaperAdventure.asVanilla(Component.text(subtitle)) as net.minecraft.network.chat.Component))
    }
  }

  override fun sendActionBar(player: Player, message: String) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundSystemChatPacket(
        PaperAdventure.asVanilla(Component.text(message)) as net.minecraft.network.chat.Component,
        true
      )
    )
  }

  override fun sendSound(
    player: Player,
    type: Sound,
    volume: Float,
    pitch: Float,
    relative: Boolean,
    offset: Vector
  ) {
    val craftPlayer = player as CraftPlayer

    @Suppress("DEPRECATION", "removal")
    val key = CraftNamespacedKey.toMinecraft(type.key)

    @Suppress("UNCHECKED_CAST")
    val soundEvent = BuiltInRegistries.SOUND_EVENT.get(key).orElseThrow()

    val pos = if (relative) {
      player.location.add(offset)
    } else {
      player.location.clone().add(offset)
    }

    craftPlayer.handle.connection.send(
      ClientboundSoundPacket(
        soundEvent,
        SoundSource.MASTER,
        pos.x,
        pos.y,
        pos.z,
        volume,
        pitch,
        Random().nextLong()
      )
    )
  }
}
