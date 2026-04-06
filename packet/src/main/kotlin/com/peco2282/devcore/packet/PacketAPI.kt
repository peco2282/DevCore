package com.peco2282.devcore.packet

import com.peco2282.devcore.packet.entity.EntityManagerHub
import com.peco2282.devcore.packet.environment.EnvironmentHub
import com.peco2282.devcore.packet.environment.FakeWorldBorderBuilder
import com.peco2282.devcore.packet.environment.WorldBorderBuilder
import com.peco2282.devcore.packet.interact.FakeBlockBuilder
import com.peco2282.devcore.packet.interact.InteractHub
import com.peco2282.devcore.packet.inventory.InventoryHub
import com.peco2282.devcore.packet.vfx.VfxHub
import com.peco2282.devcore.packet.view.ViewHub
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

/**
 * Singleton facade that delegates all [PacketHub] operations to the version-specific
 * [PacketHub] implementation loaded at runtime.
 */
object PacketAPI: PacketHub {
  private var delegate: PacketHub? = null

  /** Access to connection-related packet operations. */
  val connection: ConnectionHub get() = this
  /** Access to entity-related packet operations. */
  val entity: EntityManagerHub get() = this
  /** Access to environment-related packet operations. */
  val environment: EnvironmentHub get() = this
  /** Access to interaction-related packet operations. */
  val interact: InteractHub get() = this
  /** Access to inventory-related packet operations. */
  val inventory: InventoryHub get() = this
  /** Access to visual effect packet operations. */
  val vfx: VfxHub get() = this
  /** Access to view and perspective packet operations. */
  val view: ViewHub get() = this
  /** Access to miscellaneous packet operations. */
  val misc: MiscHub get() = this

  private fun requireDelegate(): PacketHub = delegate ?: throw UnsupportedOperationException("Packet NMS implementation not loaded for this version")

  /** Builds the fully-qualified class name for the version-specific [PacketHub] implementation. */
  private fun className(version: String) =
    "com.peco2282.devcore.packet.v${version.replace(".", "_")}.PacketHubImpl"

  /**
   * Resolves and loads the version-specific [PacketHub] implementation for the running server.
   *
   * Supported ranges: 1.20.4–1.20.6, 1.20.6–1.21.4, 1.21.4–1.21.11, 1.21.11–1.22.
   * Logs a warning if the version is unsupported or the class cannot be instantiated.
   *
   * @param plugin The owning plugin used for logging.
   */
  fun init(plugin: Plugin) {
    val version = Version(Bukkit.getMinecraftVersion())
    val className = when (version) {
      in Version("1.20.4")..<Version("1.20.6") -> className("1.20.4")
      in Version("1.20.6")..<Version("1.21.4") -> className("1.20.6")
      in Version("1.21.4")..<Version("1.21.11") -> className("1.21.4")
      in Version("1.21.11")..<Version("1.22") -> className("1.21.11")
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
      plugin.logger.info("Packet API initialized for version: ${delegate?.javaClass}")
    }
  }

  override fun injectPlayer(player: Player) = delegate?.injectPlayer(player) ?: throw UnsupportedOperationException("Player injection is not supported on this version")
  override fun removePlayer(player: Player) = delegate?.removePlayer(player) ?: throw UnsupportedOperationException("Player removal is not supported on this version")
  override fun sendPacket(player: Player, packet: Any) = delegate?.sendPacket(player, packet) ?: throw UnsupportedOperationException("Packet sending is not supported on this version")

  /**
   * Creates a new [PacketListener] instance backed by the global Netty pipeline hooks.
   */
  fun createPacketListener(): PacketListener = Packets.createPacketListener()

  override fun createFakeEntityBuilder(player: Player, type: EntityType, location: Location): FakeEntityBuilder {
    return delegate?.createFakeEntityBuilder(player, type, location)
      ?: throw UnsupportedOperationException("FakeEntityBuilder is not supported on this version")
  }

  override fun getNetworkSettings(player: Player): NetworkSettings = delegate?.getNetworkSettings(player) ?: Packets.NetworkSettingsImpl()

  override fun sendRawPacket(player: Player, channel: String, buf: ByteBuf) {
    delegate?.sendRawPacket(player, channel, buf)
  }

  override fun getCoroutineDispatcher(player: Player): CoroutineDispatcher = delegate?.getCoroutineDispatcher(player) ?: throw UnsupportedOperationException("CoroutineDispatcher is not supported on this version")

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

  override fun sendMetadata(player: Player, entityId: Int, data: MetadataBuilder.() -> Unit) {
    delegate?.sendMetadata(player, entityId, data)
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
    slot: EquipmentSlot,
    item: ItemStack
  ) {
    delegate?.fakeEquipment(player, entityId, slot, item)
  }

  override fun fakePlayerName(player: Player, target: Player, name: String) {
    delegate?.fakePlayerName(player, target, name)
  }

  override fun updateInventoryTitle(player: Player, title: String) {
    delegate?.updateInventoryTitle(player, title)
  }

  override fun fakeItemSlot(player: Player, windowId: Int, slot: Int, item: ItemStack) {
    delegate?.fakeItemSlot(player, windowId, slot, item)
  }

  override fun fakeFurnaceProgress(player: Player, cookProgress: Int, fuelProgress: Int) {
    delegate?.fakeFurnaceProgress(player, cookProgress, fuelProgress)
  }

  override fun setFakeWeather(player: Player, rain: Boolean, thunder: Boolean) {
    delegate?.setFakeWeather(player, rain, thunder)
  }
  override fun setWeatherLevel(player: Player, rainLevel: Float, thunderLevel: Float) {
    delegate?.setWeatherLevel(player, rainLevel, thunderLevel)
  }
  override fun setFakeTime(player: Player, time: Long, locked: Boolean) {
    delegate?.setFakeTime(player, time, locked)
  }
  override fun setFakeBiome(player: Player, biomeKey: String) {
    delegate?.setFakeBiome(player, biomeKey)
  }
  override fun resetWorldBorder(player: Player) {
    delegate?.resetWorldBorder(player)
  }
  override fun setFakeWorldBorder(player: Player, builder: FakeWorldBorderBuilder.() -> Unit) {
    delegate?.setFakeWorldBorder(player, builder)
  }

  override fun setFakeSkyColor(player: Player, color: Int) {
    delegate?.setFakeSkyColor(player, color)
  }

  override fun setEatingAnimation(
    player: Player,
    entityId: Int
  ) {
    delegate?.setEatingAnimation(player, entityId)
  }

  override fun setBowAnimation(player: Player, entityId: Int) {
    delegate?.setBowAnimation(player, entityId)
  }

  override fun setGuardPose(player: Player, entityId: Int) {
    delegate?.setGuardPose(player, entityId)
  }

  override fun setSleepAnimation(player: Player, entityId: Int, location: Location) {
    delegate?.setSleepAnimation(player, entityId, location)
  }

  override fun setEntityMotion(player: Player, entityId: Int, velocity: Vector) {
    delegate?.setEntityMotion(player, entityId, velocity)
  }

  override fun fakeStatistic(player: Player, statistic: org.bukkit.Statistic, value: Int) {
    delegate?.fakeStatistic(player, statistic, value)
  }

  override fun fakeExperienceBar(player: Player, bar: Float, level: Int, experience: Int) {
    delegate?.fakeExperienceBar(player, bar, level, experience)
  }

  override fun setItemCooldown(player: Player, item: org.bukkit.Material, ticks: Int) {
    delegate?.setItemCooldown(player, item, ticks)
  }

  override fun showFakeDeathScreen(player: Player, message: String) {
    delegate?.showFakeDeathScreen(player, message)
  }

  // --- view ---
  override fun setCameraEntity(player: Player, entityId: Int) {
    delegate?.setCameraEntity(player, entityId)
  }
  override fun resetCamera(player: Player) {
    delegate?.resetCamera(player)
  }
  override fun setEntityGlowing(player: Player, entityId: Int, glowing: Boolean) {
    delegate?.setEntityGlowing(player, entityId, glowing)
  }
  override fun transformEntityType(player: Player, entityId: Int, type: EntityType) {
    delegate?.transformEntityType(player, entityId, type)
  }
  override fun setEntityScale(player: Player, entityId: Int, scale: Float) {
    delegate?.setEntityScale(player, entityId, scale)
  }
  override fun setEntityUpsideDown(player: Player, entityId: Int, upsideDown: Boolean) {
    delegate?.setEntityUpsideDown(player, entityId, upsideDown)
  }

  // --- interact ---
  override fun placeFakeBlock(player: Player, location: Location, material: Material) {
    delegate?.placeFakeBlock(player, location, material)
  }
  override fun removeFakeBlock(player: Player, location: Location) {
    delegate?.removeFakeBlock(player, location)
  }
  override fun lockInventorySlot(player: Player, slot: Int, item: ItemStack?) {
    delegate?.lockInventorySlot(player, slot, item)
  }
  override fun forceHeldSlot(player: Player, slot: Int) {
    delegate?.forceHeldSlot(player, slot)
  }
  override fun showCredits(player: Player) {
    delegate?.showCredits(player)
  }
  override fun hideCredits(player: Player) {
    delegate?.hideCredits(player)
  }

  // --- vfx ---
  override fun setBlockCrack(player: Player, location: Location, stage: Int) {
    delegate?.setBlockCrack(player, location, stage)
  }
  override fun setEntityOnFire(player: Player, entityId: Int, onFire: Boolean) {
    delegate?.setEntityOnFire(player, entityId, onFire)
  }
  override fun fakeExplosion(player: Player, location: Location, power: Float) {
    delegate?.fakeExplosion(player, location, power)
  }
  override fun fakeLightning(player: Player, location: Location) {
    delegate?.fakeLightning(player, location)
  }
  override fun localSound(player: Player, sound: Sound, location: Location, volume: Float, pitch: Float) {
    delegate?.localSound(player, sound, location, volume, pitch)
  }
}