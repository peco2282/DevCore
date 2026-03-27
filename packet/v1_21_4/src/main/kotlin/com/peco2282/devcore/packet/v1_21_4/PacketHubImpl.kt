package com.peco2282.devcore.packet.v1_21_4

import com.peco2282.devcore.packet.*
import io.netty.buffer.ByteBuf
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component as VanillaComponent
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Bukkit
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.craftbukkit.util.CraftNamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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
      SectionPos.of(pos)
    }

    for ((sectionPos, blockEntries) in sections) {
      val shortPosArray = ShortArray(blockEntries.size)
      val stateArray = arrayOfNulls<BlockState>(blockEntries.size)
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
          stateArray as Array<BlockState>
        )
      )
    }
  }

  override fun sendRawPacket(player: Player, channel: String, buf: ByteBuf) {
    val friendlyByteBuf = if (buf is FriendlyByteBuf) buf else FriendlyByteBuf(buf)
    val plugin = Bukkit.getPluginManager().getPlugin("DevCore")!!
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
      connection.send(ClientboundSetTitleTextPacket(PaperAdventure.asVanilla(Component.text(title)) as VanillaComponent))
    }
    if (subtitle.isNotEmpty()) {
      connection.send(ClientboundSetSubtitleTextPacket(PaperAdventure.asVanilla(Component.text(subtitle)) as VanillaComponent))
    }
  }

  override fun sendActionBar(player: Player, message: String) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundSystemChatPacket(
        PaperAdventure.asVanilla(Component.text(message)) as VanillaComponent,
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

  override fun sendCamera(player: Player, entityId: Int) {
    val packet = ClientboundSetCameraPacket((player as CraftPlayer).handle)
    packet.setFieldValue("entityId", entityId)
    player.handle.connection.send(packet as Packet<*>)
  }

  override fun sendWorldBorder(player: Player, builder: WorldBorderBuilder.() -> Unit) {
    val wb = object : WorldBorderBuilder {
      override var x: Double = 0.0
      override var z: Double = 0.0
      override var size: Double = 30000000.0
      override var oldSize: Double = 30000000.0
      override var lerpTime: Long = 0
      override var warningDistance: Int = 5
      override var warningTime: Int = 15
    }
    wb.builder()
    val packet = Class.forName("net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket")
      .getDeclaredConstructor()
      .apply { isAccessible = true }
      .newInstance()
    packet.setFieldValue("newCenterX", wb.x)
    packet.setFieldValue("newCenterZ", wb.z)
    packet.setFieldValue("oldSize", wb.oldSize)
    packet.setFieldValue("newSize", wb.size)
    packet.setFieldValue("lerpTime", wb.lerpTime)
    packet.setFieldValue("newAbsoluteMaxSize", 30000000)
    packet.setFieldValue("warningTime", wb.warningTime)
    packet.setFieldValue("warningBlocks", wb.warningDistance)
    (player as CraftPlayer).handle.connection.send(packet as Packet<*>)
  }

  override fun sendOpenSign(player: Player, location: Location, front: Boolean) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundOpenSignEditorPacket(
        BlockPos(location.blockX, location.blockY, location.blockZ),
        front
      )
    )
  }

  override fun hideEntity(player: Player, entityId: Int) {
    (player as CraftPlayer).handle.connection.send(ClientboundRemoveEntitiesPacket(entityId))
  }

  override fun showEntity(player: Player, entityId: Int) {
    // Requires re-sending spawn packet
  }

  override fun fakeEquipment(
    player: Player,
    entityId: Int,
    slot: org.bukkit.inventory.EquipmentSlot,
    item: ItemStack
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
    val nmsItem = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(item)
    (player as CraftPlayer).handle.connection.send(
      ClientboundSetEquipmentPacket(
        entityId,
        listOf(com.mojang.datafixers.util.Pair.of(nmsSlot, nmsItem))
      )
    )
  }

  override fun sendMetadata(player: Player, entityId: Int, builder: MetadataBuilder.() -> Unit) {
    val metadataHandler = object : MetadataBuilder {
      val dataValues = mutableListOf<SynchedEntityData.DataValue<*>>()

      override fun <T> set(index: Int, type: MetadataType, value: T) {
        val serializer = when (type) {
          MetadataType.BYTE -> EntityDataSerializers.BYTE
          MetadataType.INT -> EntityDataSerializers.INT
          MetadataType.FLOAT -> EntityDataSerializers.FLOAT
          MetadataType.STRING -> EntityDataSerializers.STRING
          MetadataType.CHAT -> EntityDataSerializers.COMPONENT
          MetadataType.OPTCHAT -> EntityDataSerializers.OPTIONAL_COMPONENT
          MetadataType.ITEM -> EntityDataSerializers.ITEM_STACK
          MetadataType.BOOLEAN -> EntityDataSerializers.BOOLEAN
          MetadataType.ROTATION -> EntityDataSerializers.ROTATIONS
          MetadataType.POSITION -> EntityDataSerializers.BLOCK_POS
          MetadataType.OPTPOSITION -> EntityDataSerializers.OPTIONAL_BLOCK_POS
          MetadataType.DIRECTION -> EntityDataSerializers.DIRECTION
          MetadataType.OPTUUID -> EntityDataSerializers.OPTIONAL_UUID
          MetadataType.BLOCKID -> EntityDataSerializers.BLOCK_STATE
          MetadataType.OPTBLOCKID -> EntityDataSerializers.OPTIONAL_BLOCK_STATE
          MetadataType.NBT -> EntityDataSerializers.COMPOUND_TAG
          MetadataType.PARTICLE -> EntityDataSerializers.PARTICLE
          MetadataType.VILLAGER -> EntityDataSerializers.VILLAGER_DATA
          MetadataType.OPTINT -> EntityDataSerializers.OPTIONAL_UNSIGNED_INT
          MetadataType.POSE -> EntityDataSerializers.POSE
          MetadataType.CAT_VARIANT -> EntityDataSerializers.CAT_VARIANT
          MetadataType.FROG_VARIANT -> EntityDataSerializers.FROG_VARIANT
          MetadataType.OPT_GLOBAL_POS -> EntityDataSerializers.OPTIONAL_GLOBAL_POS
          MetadataType.PAINTING_VARIANT -> EntityDataSerializers.PAINTING_VARIANT
          MetadataType.SNIFFER_STATE -> EntityDataSerializers.SNIFFER_STATE
          MetadataType.VECTOR3 -> EntityDataSerializers.VECTOR3
          MetadataType.QUATERNION -> EntityDataSerializers.QUATERNION
        }
        @Suppress("UNCHECKED_CAST")
        dataValues.add(SynchedEntityData.DataValue(index, serializer as EntityDataSerializer<Any>, value))
      }

      override fun setGlowing(glowing: Boolean) {
        set(0, MetadataType.BYTE, (if (glowing) 0x40 else 0).toByte())
      }

      override fun setCustomName(name: String?) {
        set(2, MetadataType.OPTCHAT, (name?.let { PaperAdventure.asVanilla(Component.text(it)) }))
      }

      override fun setInvisible(invisible: Boolean) {
        set(0, MetadataType.BYTE, (if (invisible) 0x20 else 0).toByte())
      }
    }
    metadataHandler.builder()
    (player as CraftPlayer).handle.connection.send(
      ClientboundSetEntityDataPacket(entityId, metadataHandler.dataValues)
    )
  }

  override fun fakePlayerName(player: Player, target: Player, newName: String) {
    // Info Update entry logic...
  }

  override fun updateInventoryTitle(player: Player, title: String) {
    val craftPlayer = player as CraftPlayer
    val container = craftPlayer.handle.containerMenu
    val type = container.type
    val titleComponent = PaperAdventure.asVanilla(Component.text(title)) as net.minecraft.network.chat.Component
    craftPlayer.handle.connection.send(ClientboundOpenScreenPacket(container.containerId, type, titleComponent))
  }

  override fun fakeItemSlot(player: Player, windowId: Int, slot: Int, item: ItemStack) {
    val nmsItem = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(item)
    (player as CraftPlayer).handle.connection.send(ClientboundContainerSetSlotPacket(windowId, 0, slot, nmsItem))
  }

  override fun fakeFurnaceProgress(player: Player, windowId: Int, progress: Int, maxProgress: Int) {
    val connection = (player as CraftPlayer).handle.connection
    connection.send(ClientboundContainerSetDataPacket(windowId, 2, progress))
    connection.send(ClientboundContainerSetDataPacket(windowId, 3, maxProgress))
  }

  override fun setFakeWorldBorder(
    player: Player,
    size: Double,
    centerX: Double,
    centerZ: Double,
    warningBlocks: Int,
    warningTime: Int
  ) {
    val connection = (player as CraftPlayer).handle.connection
    // Property packets...
  }

  override fun setFakeWeather(player: Player, rain: Boolean, thunder: Boolean) {
    val connection = (player as CraftPlayer).handle.connection
    if (rain) {
      connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.START_RAINING, 0f))
    } else {
      connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0f))
    }
  }

  override fun setFakeSkyColor(player: Player, color: Int) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundGameEventPacket(
        ClientboundGameEventPacket.RAIN_LEVEL_CHANGE,
        color.toFloat()
      )
    )
  }

  override fun setCamera(player: Player, entityId: Int) {
    val craftPlayer = player as CraftPlayer
    val entity = craftPlayer.handle.level().getEntity(entityId) ?: return
    craftPlayer.handle.connection.send(ClientboundSetCameraPacket(entity))
  }

  override fun setEatingAnimation(
    player: Player,
    entityId: Int,
    eating: Boolean,
    item: ItemStack?
  ) {
  }

  override fun setBowAnimation(player: Player, entityId: Int, pulling: Boolean) {
  }

  override fun setGuardPose(player: Player, entityId: Int, guarding: Boolean) {
  }

  override fun setSleepAnimation(player: Player, entityId: Int, sleeping: Boolean, bedLocation: org.bukkit.Location?) {
  }

  override fun setEntityMotion(player: Player, entityId: Int, velocity: Vector) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundSetEntityMotionPacket(
        entityId,
        Vec3(velocity.x, velocity.y, velocity.z)
      )
    )
  }

  override fun fakeStatistic(player: Player, category: String, statistic: String, value: Int) {
  }

  override fun fakeExperienceBar(player: Player, level: Int, progress: Float) {
    (player as CraftPlayer).handle.connection.send(ClientboundSetExperiencePacket(progress, 0, level))
  }

  override fun setItemCooldown(player: Player, material: org.bukkit.Material, ticks: Int) {
    val nmsItem = CraftMagicNumbers.getItem(material)
    (player as CraftPlayer).handle.connection.send(ClientboundCooldownPacket(BuiltInRegistries.ITEM.getKey(nmsItem), ticks))
  }

  override fun showFakeDeathScreen(player: Player, message: String) {
  }
}
