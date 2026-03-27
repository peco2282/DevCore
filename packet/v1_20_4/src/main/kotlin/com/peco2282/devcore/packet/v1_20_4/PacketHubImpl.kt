package com.peco2282.devcore.packet.v1_20_4

import com.peco2282.devcore.packet.*
import io.netty.buffer.ByteBuf
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector

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
    player.spawnParticle(type, location, amount, offset.x, offset.y, offset.z, extra, data)
  }

  override fun sendFakeBlocks(player: Player, builder: FakeBlockBuilder.() -> Unit) {
    val connection = (player as CraftPlayer).handle.connection
    val handler = object : FakeBlockBuilder {
      val blocks = mutableMapOf<BlockPos, BlockState>()

      override fun set(location: Location, material: Material) {
        val pos = BlockPos(location.blockX, location.blockY, location.blockZ)
        blocks[pos] =
          (Bukkit.createBlockData(material) as CraftBlockData).state
      }

      override fun fill(from: Location, to: Location, material: Material) {
        val minX = minOf(from.blockX, to.blockX)
        val maxX = maxOf(from.blockX, to.blockX)
        val minY = minOf(from.blockY, to.blockY)
        val maxY = maxOf(from.blockY, to.blockY)
        val minZ = minOf(from.blockZ, to.blockZ)
        val maxZ = maxOf(from.blockZ, to.blockZ)

        val state =
          (Bukkit.createBlockData(material) as CraftBlockData).state
        for (x in minX..maxX) {
          for (y in minY..maxY) {
            for (z in minZ..maxZ) {
              val pos = BlockPos(x, y, z)
              blocks[pos] = state
            }
          }
        }
      }
    }
    handler.apply(builder)

    handler.blocks.forEach { (pos, state) ->
      connection.send(ClientboundBlockUpdatePacket(pos, state))
    }
  }

  override fun sendRawPacket(player: Player, channel: String, buf: ByteBuf) {
    val friendlyByteBuf = if (buf is FriendlyByteBuf) buf else FriendlyByteBuf(buf)
    player.sendPluginMessage(Bukkit.getPluginManager().getPlugin("DevCore")!!, channel, friendlyByteBuf.array())
  }

  override fun getCoroutineDispatcher(player: Player): CoroutineDispatcher? {
    val craftPlayer = player as CraftPlayer
    return craftPlayer.handle.connection.connection.channel.eventLoop().asCoroutineDispatcher()
  }

  override fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
    val connection = (player as CraftPlayer).handle.connection
    connection.send(ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut))
    connection.send(ClientboundSetTitleTextPacket(PaperAdventure.asVanilla(Component.text(title))))
    connection.send(ClientboundSetSubtitleTextPacket(PaperAdventure.asVanilla(Component.text(subtitle))))
  }

  override fun sendActionBar(player: Player, message: String) {
    player.sendActionBar(Component.text(message))
  }

  override fun sendSound(
    player: Player,
    type: Sound,
    volume: Float,
    pitch: Float,
    relative: Boolean,
    offset: Vector
  ) {
    val loc = player.location
    player.playSound(loc.clone().add(offset), type, volume, pitch)
  }

  override fun sendCamera(player: Player, entityId: Int) {}
  override fun sendWorldBorder(player: Player, builder: WorldBorderBuilder.() -> Unit) {}
  override fun sendOpenSign(player: Player, location: Location, front: Boolean) {}
  override fun sendMetadata(player: Player, entityId: Int, builder: MetadataBuilder.() -> Unit) {}

  override fun hideEntity(player: Player, entityId: Int) {
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket(entityId)
    )
  }

  override fun showEntity(player: Player, entityId: Int) {}

  override fun fakeEquipment(
    player: Player,
    entityId: Int,
    slot: org.bukkit.inventory.EquipmentSlot,
    item: org.bukkit.inventory.ItemStack
  ) {
    val nmsSlot = when (slot) {
      org.bukkit.inventory.EquipmentSlot.HAND -> net.minecraft.world.entity.EquipmentSlot.MAINHAND
      org.bukkit.inventory.EquipmentSlot.OFF_HAND -> net.minecraft.world.entity.EquipmentSlot.OFFHAND
      org.bukkit.inventory.EquipmentSlot.FEET -> net.minecraft.world.entity.EquipmentSlot.FEET
      org.bukkit.inventory.EquipmentSlot.LEGS -> net.minecraft.world.entity.EquipmentSlot.LEGS
      org.bukkit.inventory.EquipmentSlot.CHEST -> net.minecraft.world.entity.EquipmentSlot.CHEST
      org.bukkit.inventory.EquipmentSlot.HEAD -> net.minecraft.world.entity.EquipmentSlot.HEAD
      else -> net.minecraft.world.entity.EquipmentSlot.MAINHAND
    }
    val nmsItem = CraftItemStack.asNMSCopy(item)
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket(
        entityId,
        listOf(com.mojang.datafixers.util.Pair.of(nmsSlot, nmsItem))
      )
    )
  }

  override fun fakePlayerName(player: Player, target: Player, newName: String) {}

  override fun updateInventoryTitle(player: Player, title: String) {
    val craftPlayer = player as CraftPlayer
    val container = craftPlayer.handle.containerMenu
    val type = container.type
    val titleComponent = PaperAdventure.asVanilla(Component.text(title))
    craftPlayer.handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundOpenScreenPacket(
        container.containerId,
        type,
        titleComponent
      )
    )
  }

  override fun fakeItemSlot(player: Player, windowId: Int, slot: Int, item: org.bukkit.inventory.ItemStack) {
    val nmsItem = CraftItemStack.asNMSCopy(item)
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
        windowId,
        0,
        slot,
        nmsItem
      )
    )
  }

  override fun fakeFurnaceProgress(player: Player, windowId: Int, progress: Int, maxProgress: Int) {
    val connection = (player as CraftPlayer).handle.connection
    connection.send(net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket(windowId, 2, progress))
    connection.send(net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket(windowId, 3, maxProgress))
  }

  override fun setFakeWorldBorder(
    player: Player,
    size: Double,
    centerX: Double,
    centerZ: Double,
    warningBlocks: Int,
    warningTime: Int
  ) {
  }

  override fun setFakeWeather(player: Player, rain: Boolean, thunder: Boolean) {
    val connection = (player as CraftPlayer).handle.connection
    if (rain) {
      connection.send(
        net.minecraft.network.protocol.game.ClientboundGameEventPacket(
          net.minecraft.network.protocol.game.ClientboundGameEventPacket.START_RAINING,
          0f
        )
      )
    } else {
      connection.send(
        net.minecraft.network.protocol.game.ClientboundGameEventPacket(
          net.minecraft.network.protocol.game.ClientboundGameEventPacket.STOP_RAINING,
          0f
        )
      )
    }
  }

  override fun setFakeSkyColor(player: Player, color: Int) {
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundGameEventPacket(
        net.minecraft.network.protocol.game.ClientboundGameEventPacket.RAIN_LEVEL_CHANGE,
        color.toFloat()
      )
    )
  }

  override fun setCamera(player: Player, entityId: Int) {
    val craftPlayer = player as CraftPlayer
    val entity = craftPlayer.handle.level().getEntity(entityId) ?: return
    craftPlayer.handle.connection.send(net.minecraft.network.protocol.game.ClientboundSetCameraPacket(entity))
  }

  override fun setEatingAnimation(
    player: Player,
    entityId: Int,
    eating: Boolean,
    item: org.bukkit.inventory.ItemStack?
  ) {
  }

  override fun setBowAnimation(player: Player, entityId: Int, pulling: Boolean) {}
  override fun setGuardPose(player: Player, entityId: Int, guarding: Boolean) {}
  override fun setSleepAnimation(player: Player, entityId: Int, sleeping: Boolean, bedLocation: org.bukkit.Location?) {}

  override fun setEntityMotion(player: Player, entityId: Int, velocity: Vector) {
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(
        entityId,
        net.minecraft.world.phys.Vec3(velocity.x, velocity.y, velocity.z)
      )
    )
  }

  override fun fakeStatistic(player: Player, category: String, statistic: String, value: Int) {}

  override fun fakeExperienceBar(player: Player, level: Int, progress: Float) {
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundSetExperiencePacket(
        progress,
        0,
        level
      )
    )
  }

  override fun setItemCooldown(player: Player, material: org.bukkit.Material, ticks: Int) {
    val nmsItem = org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers.getItem(material)
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundCooldownPacket(
        nmsItem,
        ticks
      )
    )
  }

  override fun showFakeDeathScreen(player: Player, message: String) {}
}
