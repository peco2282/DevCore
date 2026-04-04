package com.peco2282.devcore.packet.v1_21_11

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
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
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
        blocks[pos] = (material as CraftBlockData).state
      }

      override fun fill(from: Location, to: Location, material: Material) {
        val minX = minOf(from.blockX, to.blockX)
        val maxX = maxOf(from.blockX, to.blockX)
        val minY = minOf(from.blockY, to.blockY)
        val maxY = maxOf(from.blockY, to.blockY)
        val minZ = minOf(from.blockZ, to.blockZ)
        val maxZ = maxOf(from.blockZ, to.blockZ)

        val state = (material as CraftBlockData).state
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

  override fun fakePlayerName(player: Player, target: Player, newName: String) {
    val craftTarget = target as CraftPlayer
    val profile = craftTarget.handle.gameProfile
    val removeEntry = net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket(listOf(profile.id))
    val addEntry = net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(
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
    if (eating && item != null) {
      val nmsItem = CraftItemStack.asNMSCopy(item)
      sendMetadata(player, entityId) { set(8, MetadataType.ITEM, nmsItem) }
    } else {
      sendMetadata(player, entityId) { set(8, MetadataType.ITEM, net.minecraft.world.item.ItemStack.EMPTY) }
    }
  }

  override fun setBowAnimation(player: Player, entityId: Int, pulling: Boolean) {
    sendMetadata(player, entityId) {
      set(8, MetadataType.ITEM, if (pulling)
        CraftItemStack.asNMSCopy(org.bukkit.inventory.ItemStack(Material.BOW))
      else
        net.minecraft.world.item.ItemStack.EMPTY
      )
    }
  }
  override fun setGuardPose(player: Player, entityId: Int, guarding: Boolean) {
    sendMetadata(player, entityId) {
      set(8, MetadataType.BYTE, (if (guarding) 0x01.or(0x02) else 0x00).toByte())
    }
  }
  override fun setSleepAnimation(player: Player, entityId: Int, sleeping: Boolean, bedLocation: org.bukkit.Location?) {
    sendMetadata(player, entityId) {
      if (sleeping && bedLocation != null) {
        val pos = net.minecraft.core.BlockPos(bedLocation.blockX, bedLocation.blockY, bedLocation.blockZ)
        set(14, MetadataType.OPTPOSITION, java.util.Optional.of(pos))
      } else {
        set(14, MetadataType.OPTPOSITION, java.util.Optional.empty<net.minecraft.core.BlockPos>())
      }
    }
  }

  override fun setEntityMotion(player: Player, entityId: Int, velocity: Vector) {
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(
        entityId,
        net.minecraft.world.phys.Vec3(velocity.x, velocity.y, velocity.z)
      )
    )
  }

  override fun fakeStatistic(player: Player, category: String, statistic: String, value: Int) {
    val nsKey = org.bukkit.NamespacedKey.fromString(statistic) ?: return
    val minecraftKey = org.bukkit.craftbukkit.util.CraftNamespacedKey.toMinecraft(nsKey)
    val stat = net.minecraft.stats.Stats.CUSTOM.get(minecraftKey) ?: return
    @Suppress("UNCHECKED_CAST")
    val statsMap = it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap<net.minecraft.stats.Stat<*>>()
    statsMap[stat as net.minecraft.stats.Stat<*>] = value
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundAwardStatsPacket(statsMap)
    )
  }

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
    val nmsItem = org.bukkit.craftbukkit.util.CraftMagicNumbers.getItem(material)
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundCooldownPacket(
        net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(nmsItem),
        ticks
      )
    )
  }

  override fun showFakeDeathScreen(player: Player, message: String) {
    val component = PaperAdventure.asVanilla(Component.text(message))
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket(player.handle.id, component)
    )
  }

  // --- environment ---
  override fun setWeatherLevel(player: Player, rainLevel: Float, thunderLevel: Float) {
    val connection = (player as CraftPlayer).handle.connection
    connection.send(net.minecraft.network.protocol.game.ClientboundGameEventPacket(net.minecraft.network.protocol.game.ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, rainLevel))
    connection.send(net.minecraft.network.protocol.game.ClientboundGameEventPacket(net.minecraft.network.protocol.game.ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thunderLevel))
  }

  override fun setFakeTime(player: Player, time: Long, locked: Boolean) {
    val connection = (player as CraftPlayer).handle.connection
    val dayTime = if (locked) -time else time
    connection.send(net.minecraft.network.protocol.game.ClientboundSetTimePacket(0L, dayTime, !locked))
  }

  override fun setFakeBiome(player: Player, biomeKey: String) {
  }

  override fun resetWorldBorder(player: Player) {
    val connection = (player as CraftPlayer).handle.connection
    val serverBorder = player.handle.level().worldBorder
    connection.send(net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket(serverBorder))
  }

  override fun setFakeWorldBorder(player: Player, builder: com.peco2282.devcore.packet.environment.FakeWorldBorderBuilder.() -> Unit) {
    val wb = object : com.peco2282.devcore.packet.environment.FakeWorldBorderBuilder {
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
    (player as CraftPlayer).handle.connection.send(packet as net.minecraft.network.protocol.Packet<*>)
  }

  // --- view ---
  override fun setCameraEntity(player: Player, entityId: Int) {
    val craftPlayer = player as CraftPlayer
    val entity = craftPlayer.handle.level().getEntity(entityId) ?: return
    craftPlayer.handle.connection.send(net.minecraft.network.protocol.game.ClientboundSetCameraPacket(entity))
  }

  override fun resetCamera(player: Player) {
    val craftPlayer = player as CraftPlayer
    craftPlayer.handle.connection.send(net.minecraft.network.protocol.game.ClientboundSetCameraPacket(craftPlayer.handle))
  }

  override fun setEntityGlowing(player: Player, entityId: Int, glowing: Boolean) {
    sendMetadata(player, entityId) { setGlowing(glowing) }
  }

  override fun transformEntityType(player: Player, entityId: Int, type: org.bukkit.entity.EntityType) {
  }

  override fun setEntityScale(player: Player, entityId: Int, scale: Float) {
    sendMetadata(player, entityId) {
      set(0, com.peco2282.devcore.packet.MetadataType.FLOAT, scale)
    }
  }

  override fun setEntityUpsideDown(player: Player, entityId: Int, upsideDown: Boolean) {
    val name = if (upsideDown) "§r\u0000" else null
    sendMetadata(player, entityId) {
      setCustomName(name)
      set(3, com.peco2282.devcore.packet.MetadataType.BOOLEAN, upsideDown)
    }
  }

  // --- interact ---
  override fun placeFakeBlock(player: Player, location: org.bukkit.Location, material: org.bukkit.Material) {
    val connection = (player as CraftPlayer).handle.connection
    val pos = net.minecraft.core.BlockPos(location.blockX, location.blockY, location.blockZ)
    val state = org.bukkit.craftbukkit.block.data.CraftBlockData.newData(material.asBlockType(), null).state
    connection.send(net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(pos, state))
  }

  override fun removeFakeBlock(player: Player, location: org.bukkit.Location) {
    val connection = (player as CraftPlayer).handle.connection
    val pos = net.minecraft.core.BlockPos(location.blockX, location.blockY, location.blockZ)
    val world = (location.world as org.bukkit.craftbukkit.CraftWorld).handle
    connection.send(net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(world, pos))
  }

  override fun lockInventorySlot(player: Player, slot: Int, item: org.bukkit.inventory.ItemStack?) {
    val nmsItem = if (item != null) org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(item)
    else net.minecraft.world.item.ItemStack.EMPTY
    val craftPlayer = player as CraftPlayer
    val windowId = craftPlayer.handle.containerMenu.containerId
    craftPlayer.handle.connection.send(net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(windowId, 0, slot, nmsItem))
  }

  override fun forceHeldSlot(player: Player, slot: Int) {
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket(slot)
    )
  }

  override fun showCredits(player: Player) {
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundGameEventPacket(net.minecraft.network.protocol.game.ClientboundGameEventPacket.WIN_GAME, 1f)
    )
  }

  override fun hideCredits(player: Player) {
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundGameEventPacket(net.minecraft.network.protocol.game.ClientboundGameEventPacket.WIN_GAME, 0f)
    )
  }

  // --- vfx ---
  override fun setBlockCrack(player: Player, location: org.bukkit.Location, stage: Int) {
    val entityId = (location.hashCode() and 0x7FFFFFFF)
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket(
        entityId,
        net.minecraft.core.BlockPos(location.blockX, location.blockY, location.blockZ),
        stage
      )
    )
  }

  override fun setEntityOnFire(player: Player, entityId: Int, onFire: Boolean) {
    sendMetadata(player, entityId) {
      set(0, com.peco2282.devcore.packet.MetadataType.BYTE, (if (onFire) 0x01 else 0x00).toByte())
    }
  }

  override fun fakeExplosion(player: Player, location: org.bukkit.Location, power: Float) {
    val craftPlayer = player as CraftPlayer
    craftPlayer.handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
        net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
        true,
        false,
        location.x, location.y, location.z,
        0f, 0f, 0f,
        0f, 1
      )
    )
  }

  override fun fakeLightning(player: Player, location: org.bukkit.Location) {
    val craftPlayer = player as CraftPlayer
    val lightningBolt = net.minecraft.world.entity.LightningBolt(
      net.minecraft.world.entity.EntityType.LIGHTNING_BOLT,
      craftPlayer.handle.level()
    )
    lightningBolt.setPos(location.x, location.y, location.z)
    craftPlayer.handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundAddEntityPacket(
        lightningBolt.id,
        lightningBolt.uuid,
        location.x, location.y, location.z,
        0f, 0f,
        lightningBolt.type,
        0,
        net.minecraft.world.phys.Vec3.ZERO,
        0.0
      )
    )
  }

  override fun localSound(player: Player, sound: org.bukkit.Sound, location: org.bukkit.Location, volume: Float, pitch: Float) {
    @Suppress("DEPRECATION", "removal")
    val key = org.bukkit.craftbukkit.util.CraftNamespacedKey.toMinecraft(sound.key)
    val soundEvent = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(key).orElseThrow()
    (player as CraftPlayer).handle.connection.send(
      net.minecraft.network.protocol.game.ClientboundSoundPacket(
        soundEvent,
        net.minecraft.sounds.SoundSource.MASTER,
        location.x, location.y, location.z,
        volume, pitch,
        java.util.Random().nextLong()
      )
    )
  }
}
