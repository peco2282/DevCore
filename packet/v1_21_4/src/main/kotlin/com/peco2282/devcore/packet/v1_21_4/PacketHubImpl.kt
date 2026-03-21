package com.peco2282.devcore.packet.v1_21_4

import com.peco2282.devcore.packet.*
import io.netty.buffer.ByteBuf
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftNamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*

class PacketHubImpl : PacketHub {
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
    return FakeEntityBuilderImpl(player, type, location)
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
    val connection = (player as CraftPlayer).handle.connection
    player.spawnParticle(type, location, amount, offset.x, offset.y, offset.z, extra, data)
  }

  override fun sendFakeBlocks(player: Player, builder: FakeBlockBuilder.() -> Unit) {
    val connection = (player as CraftPlayer).handle.connection
    val handler = object : FakeBlockBuilder {
      val blocks = mutableMapOf<BlockPos, BlockState>()

      override fun set(location: Location, material: Material) {
        val pos = BlockPos(location.blockX, location.blockY, location.blockZ)
        blocks[pos] = CraftBlockData.newData(material.asBlockType(), null).state
      }

      override fun fill(from: Location, to: Location, material: Material) {
        val minX = minOf(from.blockX, to.blockX)
        val maxX = maxOf(from.blockX, to.blockX)
        val minY = minOf(from.blockY, to.blockY)
        val maxY = maxOf(from.blockY, to.blockY)
        val minZ = minOf(from.blockZ, to.blockZ)
        val maxZ = maxOf(from.blockZ, to.blockZ)
        val state = CraftBlockData.newData(material.asBlockType(), null).state
        for (x in minX..maxX) {
          for (y in minY..maxY) {
            for (z in minZ..maxZ) {
              blocks[BlockPos(x, y, z)] = state
            }
          }
        }
      }
    }
    handler.builder()

    // Group blocks by section
    val sections = handler.blocks.entries.groupBy { (pos, _) ->
      net.minecraft.core.SectionPos.of(pos)
    }

    for ((sectionPos, blockEntries) in sections) {
      val shortPosArray = ShortArray(blockEntries.size)
      val stateArray = arrayOfNulls<net.minecraft.world.level.block.state.BlockState>(blockEntries.size)
      for (i in blockEntries.indices) {
        val entry = blockEntries[i]
        val pos = entry.key
        val state = entry.value
        shortPosArray[i] = ((pos.x and 15) shl 8 or ((pos.z and 15) shl 4) or (pos.y and 15)).toShort()
        stateArray[i] = state
      }
      connection.send(
        ClientboundSectionBlocksUpdatePacket(
          sectionPos,
          it.unimi.dsi.fastutil.shorts.ShortArraySet(shortPosArray),
          stateArray as Array<net.minecraft.world.level.block.state.BlockState>
        )
      )
    }
  }

  override fun sendRawPacket(player: Player, channel: String, buf: ByteBuf) {
    val friendlyByteBuf = if (buf is FriendlyByteBuf) buf else FriendlyByteBuf(buf)
    val plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("DevCore")!!
    val bytes = ByteArray(friendlyByteBuf.readableBytes())
    friendlyByteBuf.readBytes(bytes)
    player.sendPluginMessage(plugin, channel, bytes)
  }

  override fun getCoroutineDispatcher(player: Player): CoroutineDispatcher? {
    val craftPlayer = player as CraftPlayer
    return craftPlayer.handle.connection.connection.channel.eventLoop().asCoroutineDispatcher()
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
