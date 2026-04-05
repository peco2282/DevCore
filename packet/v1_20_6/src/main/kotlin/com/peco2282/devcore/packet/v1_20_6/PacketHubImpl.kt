package com.peco2282.devcore.packet.v1_20_6

import com.mojang.datafixers.util.Pair
import com.peco2282.devcore.packet.*
import com.peco2282.devcore.packet.environment.FakeWorldBorderBuilder
import com.peco2282.devcore.packet.environment.WorldBorderBuilder
import com.peco2282.devcore.packet.interact.FakeBlockBuilder
import io.netty.buffer.ByteBuf
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import net.kyori.adventure.text.Component
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundCooldownPacket
import net.minecraft.network.protocol.game.ClientboundExplodePacket
import net.minecraft.network.protocol.game.ClientboundGameEventPacket
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTimePacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.LightningBolt
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.Statistic
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.craftbukkit.util.CraftNamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.Optional
import java.util.Random

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
    val friendlyByteBuf = buf as? FriendlyByteBuf ?: FriendlyByteBuf(buf)
    player.sendPluginMessage(Bukkit.getPluginManager().getPlugin("DevCore")!!, channel, friendlyByteBuf.array())
  }

  override fun getCoroutineDispatcher(player: Player): CoroutineDispatcher {
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
  override fun sendMetadata(player: Player, entityId: Int, data: MetadataBuilder.() -> Unit) {}

  override fun hideEntity(player: Player, entityId: Int) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundRemoveEntitiesPacket(entityId)
    )
  }

  override fun showEntity(player: Player, entityId: Int) {}

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
    val nmsItem = CraftItemStack.asNMSCopy(item)
    (player as CraftPlayer).handle.connection.send(
      ClientboundSetEquipmentPacket(
        entityId,
        listOf(Pair.of(nmsSlot, nmsItem))
      )
    )
  }

  override fun fakePlayerName(player: Player, target: Player, newName: String) {
    val craftTarget = target as CraftPlayer
    val profile = craftTarget.handle.gameProfile
    val removeEntry = ClientboundPlayerInfoRemovePacket(listOf(profile.id))
    val addEntry = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(
      listOf(craftTarget.handle)
    )
    val connection = (player as CraftPlayer).handle.connection
    connection.send(removeEntry)
    connection.send(addEntry)
  }

  override fun updateInventoryTitle(player: Player, title: String) {
    val craftPlayer = player as CraftPlayer
    val container = craftPlayer.handle.containerMenu
    val type = container.type
    val titleComponent = PaperAdventure.asVanilla(Component.text(title))
    craftPlayer.handle.connection.send(
      ClientboundOpenScreenPacket(
        container.containerId,
        type,
        titleComponent
      )
    )
  }

  override fun fakeItemSlot(player: Player, windowId: Int, slot: Int, item: ItemStack) {
    val nmsItem = CraftItemStack.asNMSCopy(item)
    (player as CraftPlayer).handle.connection.send(
      ClientboundContainerSetSlotPacket(
        windowId,
        0,
        slot,
        nmsItem
      )
    )
  }

  override fun fakeFurnaceProgress(player: Player, cookProgress: Int, fuelProgress: Int) {
    val connection = (player as CraftPlayer).handle.connection
    connection.send(ClientboundContainerSetDataPacket(0, 2, cookProgress))
    connection.send(ClientboundContainerSetDataPacket(0, 3, fuelProgress))
  }

  override fun setFakeWeather(player: Player, rain: Boolean, thunder: Boolean) {
    val connection = (player as CraftPlayer).handle.connection
    if (rain) {
      connection.send(
        ClientboundGameEventPacket(
          ClientboundGameEventPacket.START_RAINING,
          0f
        )
      )
    } else {
      connection.send(
        ClientboundGameEventPacket(
          ClientboundGameEventPacket.STOP_RAINING,
          0f
        )
      )
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

  fun setCamera(player: Player, entityId: Int) {
    val craftPlayer = player as CraftPlayer
    val entity = craftPlayer.handle.level().getEntity(entityId) ?: return
    craftPlayer.handle.connection.send(ClientboundSetCameraPacket(entity))
  }

  override fun setEatingAnimation(player: Player, entityId: Int) {
    sendMetadata(player, entityId) { set(8, MetadataType.ITEM, net.minecraft.world.item.ItemStack.EMPTY) }
  }

  override fun setBowAnimation(player: Player, entityId: Int) {
    sendMetadata(player, entityId) { set(8, MetadataType.ITEM, net.minecraft.world.item.ItemStack.EMPTY) }
  }

  override fun setGuardPose(player: Player, entityId: Int) {
    sendMetadata(player, entityId) { set(8, MetadataType.BYTE, 0x01.toByte()) }
  }

  override fun setSleepAnimation(player: Player, entityId: Int, location: Location) {
    sendMetadata(player, entityId) {
      val pos = BlockPos(location.blockX, location.blockY, location.blockZ)
      set(14, MetadataType.OPTPOSITION, Optional.of(pos))
    }
  }

  override fun setEntityMotion(player: Player, entityId: Int, velocity: Vector) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundSetEntityMotionPacket(
        entityId,
        Vec3(velocity.x, velocity.y, velocity.z)
      )
    )
  }

  override fun fakeStatistic(player: Player, statistic: Statistic, value: Int) {}

  override fun fakeExperienceBar(player: Player, bar: Float, level: Int, experience: Int) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundSetExperiencePacket(
        bar,
        experience,
        level
      )
    )
  }

  override fun setItemCooldown(player: Player, material: Material, ticks: Int) {
    val nmsItem = CraftMagicNumbers.getItem(material)
    (player as CraftPlayer).handle.connection.send(
      ClientboundCooldownPacket(
        nmsItem,
        ticks
      )
    )
  }

  override fun showFakeDeathScreen(player: Player, message: String) {
    val component = PaperAdventure.asVanilla(Component.text(message))
    (player as CraftPlayer).handle.connection.send(
      ClientboundPlayerCombatKillPacket(player.handle.id, component)
    )
  }

  // --- environment ---
  override fun setWeatherLevel(player: Player, rainLevel: Float, thunderLevel: Float) {
    val connection = (player as CraftPlayer).handle.connection
    connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, rainLevel))
    connection.send(ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thunderLevel))
  }

  override fun setFakeTime(player: Player, time: Long, locked: Boolean) {
    val connection = (player as CraftPlayer).handle.connection
    val dayTime = if (locked) -time else time
    connection.send(ClientboundSetTimePacket(0L, dayTime, !locked))
  }

  override fun setFakeBiome(player: Player, biomeKey: String) {
  }

  override fun resetWorldBorder(player: Player) {
    val connection = (player as CraftPlayer).handle.connection
    val serverBorder = player.handle.level().worldBorder
    connection.send(ClientboundInitializeBorderPacket(serverBorder))
  }

  override fun setFakeWorldBorder(player: Player, builder: FakeWorldBorderBuilder.() -> Unit) {
    val wb = object : FakeWorldBorderBuilder {
      override var centerX: Double = 0.0
      override var centerZ: Double = 0.0
      override var size: Double = 30000000.0
      override var oldSize: Double = 30000000.0
      override var lerpTime: Long = 0L
      override var warningBlocks: Int = 5
      override var warningTime: Int = 15
    }
    wb.builder()
    val packet = Class.forName("net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket")
      .getDeclaredConstructor()
      .apply { isAccessible = true }
      .newInstance()
    packet.setFieldValue("newCenterX", wb.centerX)
    packet.setFieldValue("newCenterZ", wb.centerZ)
    packet.setFieldValue("oldSize", wb.oldSize)
    packet.setFieldValue("newSize", wb.size)
    packet.setFieldValue("lerpTime", wb.lerpTime)
    packet.setFieldValue("newAbsoluteMaxSize", 30000000)
    packet.setFieldValue("warningTime", wb.warningTime)
    packet.setFieldValue("warningBlocks", wb.warningBlocks)
    (player as CraftPlayer).handle.connection.send(packet as Packet<*>)
  }

  // --- view ---
  override fun setCameraEntity(player: Player, entityId: Int) {
    val craftPlayer = player as CraftPlayer
    val entity = craftPlayer.handle.level().getEntity(entityId) ?: return
    craftPlayer.handle.connection.send(ClientboundSetCameraPacket(entity))
  }

  override fun resetCamera(player: Player) {
    val craftPlayer = player as CraftPlayer
    craftPlayer.handle.connection.send(ClientboundSetCameraPacket(craftPlayer.handle))
  }

  override fun setEntityGlowing(player: Player, entityId: Int, glowing: Boolean) {
    sendMetadata(player, entityId) { setGlowing(glowing) }
  }

  override fun transformEntityType(player: Player, entityId: Int, type: EntityType) {
  }

  override fun setEntityScale(player: Player, entityId: Int, scale: Float) {
    sendMetadata(player, entityId) {
      set(0, MetadataType.FLOAT, scale)
    }
  }

  override fun setEntityUpsideDown(player: Player, entityId: Int, upsideDown: Boolean) {
    val name = if (upsideDown) "§r\u0000" else null
    sendMetadata(player, entityId) {
      setCustomName(name)
      set(3, MetadataType.BOOLEAN, upsideDown)
    }
  }

  // --- interact ---
  override fun placeFakeBlock(player: Player, location: Location, material: Material) {
    val connection = (player as CraftPlayer).handle.connection
    val pos = BlockPos(location.blockX, location.blockY, location.blockZ)
    val state = CraftBlockData.newData(material.asBlockType(), null).state
    connection.send(ClientboundBlockUpdatePacket(pos, state))
  }

  override fun removeFakeBlock(player: Player, location: Location) {
    val connection = (player as CraftPlayer).handle.connection
    val pos = BlockPos(location.blockX, location.blockY, location.blockZ)
    val world = (location.world as CraftWorld).handle
    connection.send(ClientboundBlockUpdatePacket(world, pos))
  }

  override fun lockInventorySlot(player: Player, slot: Int, item: ItemStack?) {
    val nmsItem = if (item != null) CraftItemStack.asNMSCopy(item) else net.minecraft.world.item.ItemStack.EMPTY
    val craftPlayer = player as CraftPlayer
    val windowId = craftPlayer.handle.containerMenu.containerId
    craftPlayer.handle.connection.send(ClientboundContainerSetSlotPacket(windowId, 0, slot, nmsItem))
  }

  override fun forceHeldSlot(player: Player, slot: Int) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundSetCarriedItemPacket(slot)
    )
  }

  override fun showCredits(player: Player) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 1f)
    )
  }

  override fun hideCredits(player: Player) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0f)
    )
  }

  // --- vfx ---
  override fun setBlockCrack(player: Player, location: Location, stage: Int) {
    val entityId = (location.hashCode() and 0x7FFFFFFF)
    (player as CraftPlayer).handle.connection.send(
      ClientboundBlockDestructionPacket(
        entityId,
        BlockPos(location.blockX, location.blockY, location.blockZ),
        stage
      )
    )
  }

  override fun setEntityOnFire(player: Player, entityId: Int, onFire: Boolean) {
    sendMetadata(player, entityId) {
      set(0, MetadataType.BYTE, (if (onFire) 0x01 else 0x00).toByte())
    }
  }

  override fun fakeExplosion(player: Player, location: Location, power: Float) {
    (player as CraftPlayer).handle.connection.send(
      ClientboundExplodePacket(
        location.x, location.y, location.z,
        power,
        listOf(),
        null,
        Explosion.BlockInteraction.KEEP,
        ParticleTypes.EXPLOSION,
        ParticleTypes.EXPLOSION_EMITTER,
        SoundEvents.GENERIC_EXPLODE
      )
    )
  }

  override fun fakeLightning(player: Player, location: Location) {
    val craftPlayer = player as CraftPlayer
    val lightningBolt = LightningBolt(
      net.minecraft.world.entity.EntityType.LIGHTNING_BOLT,
      craftPlayer.handle.level()
    )
    lightningBolt.setPos(location.x, location.y, location.z)
    craftPlayer.handle.connection.send(
      ClientboundAddEntityPacket(
        lightningBolt.id,
        lightningBolt.uuid,
        location.x, location.y, location.z,
        0f, 0f,
        lightningBolt.type,
        0,
        Vec3.ZERO,
        0.0
      )
    )
  }

  override fun localSound(player: Player, sound: Sound, location: Location, volume: Float, pitch: Float) {
    @Suppress("DEPRECATION")
    val key = CraftNamespacedKey.toMinecraft(sound.key)
    val soundEventHolder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(
      BuiltInRegistries.SOUND_EVENT.get(key) ?: return
    )
    (player as CraftPlayer).handle.connection.send(
      ClientboundSoundPacket(
        soundEventHolder,
        SoundSource.MASTER,
        location.x, location.y, location.z,
        volume, pitch,
        Random().nextLong()
      )
    )
  }
}
