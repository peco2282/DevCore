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

  fun createPacketListener(): PacketListener = Packets.createPacketListener()

  override fun createFakeEntityBuilder(player: Player, type: EntityType, location: Location): FakeEntityBuilder {
    return delegate?.createFakeEntityBuilder(player, type, location)
      ?: throw UnsupportedOperationException("FakeEntityBuilder is not supported on this version")
  }

  override fun getNetworkSettings(player: Player): NetworkSettings = delegate?.getNetworkSettings(player) ?: Packets.NetworkSettingsImpl()

  override fun sendRawPacket(player: Player, channel: String, buf: ByteBuf) {
    delegate?.sendRawPacket(player, channel, buf)
  }

  override fun getCoroutineDispatcher(player: Player): CoroutineDispatcher? = delegate?.getCoroutineDispatcher(player)

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

  override fun sendCamera(player: Player, entityId: Int) {
    delegate?.sendCamera(player, entityId)
  }

  override fun sendWorldBorder(player: Player, builder: WorldBorderBuilder.() -> Unit) {
    delegate?.sendWorldBorder(player, builder)
  }

  override fun sendOpenSign(player: Player, location: Location, front: Boolean) {
    delegate?.sendOpenSign(player, location, front)
  }

  override fun sendMetadata(player: Player, entityId: Int, builder: MetadataBuilder.() -> Unit) {
    delegate?.sendMetadata(player, entityId, builder)
  }

  override fun hideEntity(player: Player, entityId: Int) {
    delegate?.hideEntity(player, entityId)
  }

  override fun showEntity(player: Player, entityId: Int) {
    delegate?.showEntity(player, entityId)
  }

  override fun fakeEquipment(
    player: Player,
    entityId: Int,
    slot: org.bukkit.inventory.EquipmentSlot,
    item: org.bukkit.inventory.ItemStack
  ) {
    delegate?.fakeEquipment(player, entityId, slot, item)
  }

  override fun fakePlayerName(player: Player, target: Player, newName: String) {
    delegate?.fakePlayerName(player, target, newName)
  }

  override fun updateInventoryTitle(player: Player, title: String) {
    delegate?.updateInventoryTitle(player, title)
  }

  override fun fakeItemSlot(player: Player, windowId: Int, slot: Int, item: org.bukkit.inventory.ItemStack) {
    delegate?.fakeItemSlot(player, windowId, slot, item)
  }

  override fun fakeFurnaceProgress(player: Player, windowId: Int, progress: Int, maxProgress: Int) {
    delegate?.fakeFurnaceProgress(player, windowId, progress, maxProgress)
  }

  override fun setFakeWorldBorder(
    player: Player,
    size: Double,
    centerX: Double,
    centerZ: Double,
    warningBlocks: Int,
    warningTime: Int
  ) {
    delegate?.setFakeWorldBorder(player, size, centerX, centerZ, warningBlocks, warningTime)
  }

  override fun setFakeWeather(player: Player, rain: Boolean, thunder: Boolean) {
    delegate?.setFakeWeather(player, rain, thunder)
  }

  override fun setFakeSkyColor(player: Player, color: Int) {
    delegate?.setFakeSkyColor(player, color)
  }

  override fun setCamera(player: Player, entityId: Int) {
    delegate?.setCamera(player, entityId)
  }

  override fun setEatingAnimation(
    player: Player,
    entityId: Int,
    eating: Boolean,
    item: org.bukkit.inventory.ItemStack?
  ) {
    delegate?.setEatingAnimation(player, entityId, eating, item)
  }

  override fun setBowAnimation(player: Player, entityId: Int, pulling: Boolean) {
    delegate?.setBowAnimation(player, entityId, pulling)
  }

  override fun setGuardPose(player: Player, entityId: Int, guarding: Boolean) {
    delegate?.setGuardPose(player, entityId, guarding)
  }

  override fun setSleepAnimation(player: Player, entityId: Int, sleeping: Boolean, bedLocation: org.bukkit.Location?) {
    delegate?.setSleepAnimation(player, entityId, sleeping, bedLocation)
  }

  override fun setEntityMotion(player: Player, entityId: Int, velocity: org.bukkit.util.Vector) {
    delegate?.setEntityMotion(player, entityId, velocity)
  }

  override fun fakeStatistic(player: Player, category: String, statistic: String, value: Int) {
    delegate?.fakeStatistic(player, category, statistic, value)
  }

  override fun fakeExperienceBar(player: Player, level: Int, progress: Float) {
    delegate?.fakeExperienceBar(player, level, progress)
  }

  override fun setItemCooldown(player: Player, material: org.bukkit.Material, ticks: Int) {
    delegate?.setItemCooldown(player, material, ticks)
  }

  override fun showFakeDeathScreen(player: Player, message: String) {
    delegate?.showFakeDeathScreen(player, message)
  }
}