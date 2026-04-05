package com.peco2282.devcore.packet

import com.peco2282.devcore.packet.environment.FakeWorldBorderBuilder
import com.peco2282.devcore.scheduler.PluginRegistory
import com.peco2282.devcore.scheduler.ticks
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Rotation
import org.bukkit.Sound
import org.bukkit.block.BlockState
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.util.UUID

/** DSL marker annotation for the packet DSL scope. Prevents implicit receiver leaking. */
@DslMarker
annotation class PacketDsl

/**
 * Top-level DSL builder for sending various packet-based effects to a [Player].
 *
 * Obtain an instance via the [packet] top-level function.
 *
 * @param player The target player for all operations in this builder.
 */
@PacketDsl
class PacketBuilder(private val player: Player) {

  /**
   * Sends a title and subtitle to the player.
   *
   * @param builder DSL block for configuring the title.
   */
  fun title(builder: TitleBuilder.() -> Unit) {
    val titleBuilder = TitleBuilder().apply(builder)
    PacketAPI.view.sendTitle(
      player,
      titleBuilder.title,
      titleBuilder.subtitle,
      titleBuilder.fadeIn,
      titleBuilder.stay,
      titleBuilder.fadeOut
    )
  }

  /**
   * Sends an action bar message to the player.
   *
   * @param message The text to display in the action bar.
   */
  fun actionBar(message: String) = PacketAPI.view.sendActionBar(player, message)

  /**
   * Sends a sound effect to the player.
   *
   * @param builder DSL block for configuring the sound.
   */
  fun sound(builder: SoundBuilder.() -> Unit) {
    val soundBuilder = SoundBuilder().apply(builder)
    val sound = soundBuilder.type ?: return
    PacketAPI.vfx.sendSound(
      player,
      sound,
      soundBuilder.volume,
      soundBuilder.pitch,
      soundBuilder.relative,
      soundBuilder.offset
    )
  }

  /**
   * Spawns a client-side fake entity visible only to this player.
   *
   * @param type The entity type to spawn.
   * @param location The initial location of the fake entity.
   * @param builder DSL block for configuring the fake entity.
   */
  fun sendFakeEntity(type: EntityType, location: Location, builder: FakeEntityBuilder.() -> Unit) {
    val fakeEntityBuilder = PacketAPI.entity.createFakeEntityBuilder(player, type, location).apply(builder)
    fakeEntityBuilder.spawn()
  }

  /**
   * Sends a particle effect to the player.
   *
   * @param type The particle type.
   * @param builder DSL block for configuring the particle effect.
   */
  fun particles(type: Particle, builder: ParticleBuilder.() -> Unit) {
    val particleBuilder = ParticleBuilder(type).apply(builder)
    PacketAPI.vfx.sendParticles(
      player,
      particleBuilder.type,
      particleBuilder.location ?: player.location,
      particleBuilder.amount,
      particleBuilder.offset,
      particleBuilder.extra,
      particleBuilder.data
    )
  }

  /**
   * Sends fake block changes visible only to this player.
   *
   * @param builder DSL block for specifying fake block positions and materials.
   */
  fun fakeBlocks(builder: com.peco2282.devcore.packet.interact.FakeBlockBuilder.() -> Unit) {
    PacketAPI.interact.sendFakeBlocks(player, builder)
  }

  /**
   * Sets the player's camera to the specified entity.
   *
   * @param entityId The entity ID to attach the camera to.
   */
  fun camera(entityId: Int) = PacketAPI.view.sendCamera(player, entityId)

  /**
   * Sends a world border update to the player.
   *
   * @param builder DSL block for configuring the world border.
   */
  fun worldBorder(builder: com.peco2282.devcore.packet.environment.WorldBorderBuilder.() -> Unit) {
    PacketAPI.environment.sendWorldBorder(player, builder)
  }

  /**
   * Opens a sign editor for the player at the given location.
   *
   * @param location The location of the sign block.
   * @param front Whether to open the front face of the sign.
   */
  fun openSign(location: Location, front: Boolean = true) {
    PacketAPI.sendOpenSign(player, location, front)
  }

  /**
   * Sends entity metadata to the player.
   *
   * @param entityId The entity whose metadata is updated.
   * @param builder DSL block for specifying metadata entries.
   */
  fun metadata(entityId: Int, builder: MetadataBuilder.() -> Unit) {
    PacketAPI.sendMetadata(player, entityId, builder)
  }

  // --- New DSL Actions ---

  fun hideEntity(entityId: Int) = PacketAPI.hideEntity(player, entityId)
  fun showEntity(entityId: Int) = PacketAPI.showEntity(player, entityId)

  fun fakeEquipment(entityId: Int, slot: EquipmentSlot, item: ItemStack) =
    PacketAPI.fakeEquipment(player, entityId, slot, item)

  fun fakePlayerName(target: Player, newName: String) =
    PacketAPI.fakePlayerName(player, target, newName)

  fun updateInventoryTitle(title: String) =
    PacketAPI.updateInventoryTitle(player, title)

  fun fakeItemSlot(windowId: Int, slot: Int, item: ItemStack) =
    PacketAPI.fakeItemSlot(player, windowId, slot, item)

  fun fakeFurnaceProgress(cookProgress: Int, fuelProgress: Int) =
    PacketAPI.inventory.fakeFurnaceProgress(player, cookProgress, fuelProgress)

  fun fakeWorldBorder(size: Double, centerX: Double, centerZ: Double, warningBlocks: Int = 5, warningTime: Int = 15) =
    PacketAPI.environment.setFakeWorldBorder(player) {
      this.size = size
      this.centerX = centerX
      this.centerZ = centerZ
      this.warningBlocks = warningBlocks
      this.warningTime = warningTime
    }

  fun fakeWorldBorder(builder: com.peco2282.devcore.packet.environment.FakeWorldBorderBuilder.() -> Unit) =
    PacketAPI.environment.setFakeWorldBorder(player, builder)

  fun weather(rain: Boolean, thunder: Boolean = false) =
    PacketAPI.environment.setFakeWeather(player, rain, thunder)

  fun skyColor(color: Int) =
    PacketAPI.view.setFakeSkyColor(player, color)

  fun setCamera(entityId: Int) =
    PacketAPI.view.setCameraEntity(player, entityId)

  fun eatingAnimation(entityId: Int) =
    PacketAPI.entity.setEatingAnimation(player, entityId)

  fun bowAnimation(entityId: Int) =
    PacketAPI.entity.setBowAnimation(player, entityId)

  fun guardPose(entityId: Int) =
    PacketAPI.entity.setGuardPose(player, entityId)

  fun sleepAnimation(entityId: Int, location: Location) =
    PacketAPI.entity.setSleepAnimation(player, entityId, location)

  fun entityMotion(entityId: Int, velocity: Vector) =
    PacketAPI.entity.setEntityMotion(player, entityId, velocity)

  fun statistic(statistic: org.bukkit.Statistic, value: Int) =
    PacketAPI.misc.fakeStatistic(player, statistic, value)

  fun experienceBar(bar: Float, level: Int, experience: Int) =
    PacketAPI.misc.fakeExperienceBar(player, bar, level, experience)

  fun itemCooldown(material: Material, ticks: Int) =
    PacketAPI.inventory.setItemCooldown(player, material, ticks)

  fun deathScreen(message: String) =
    PacketAPI.misc.showFakeDeathScreen(player, message)
}

/**
 * DSL builder for configuring and spawning a client-side fake entity.
 *
 * Obtain an instance via [PacketBuilder.sendFakeEntity] or [PacketAPI.createFakeEntityBuilder].
 */
@PacketDsl
interface FakeEntityBuilder {
  var customName: String?
  var isCustomNameVisible: Boolean
  var isInvisible: Boolean
  var isGlowing: Boolean

  fun equipment(builder: EquipmentBuilder.() -> Unit)
  fun animate(animation: EntityAnimation)
  fun move(location: Location, onGround: Boolean = true)
  fun rotate(yaw: Float, pitch: Float, headRotation: Float? = null)
  fun updateMetadata()
  fun despawnAfter(ticks: Long)
  fun spawn()
}

/**
 * DSL builder for configuring a particle effect.
 *
 * @property type The particle type.
 * @property amount The number of particles to spawn.
 * @property offset The spread offset of the particles.
 * @property extra Extra data such as speed.
 * @property location The location to spawn particles at, or `null` to use the player's location.
 * @property data Optional particle-specific data (e.g. `DustOptions`).
 */
@PacketDsl
class ParticleBuilder(val type: Particle) {
  var amount: Int = 10
  var offset: Vector = Vector(0.5, 0.5, 0.5)
  var extra: Double = 0.1
  var location: Location? = null
  var data: Any? = null
}

/**
 * DSL builder for specifying equipment items for a fake entity.
 */
@PacketDsl
class EquipmentBuilder {
  var mainHand: ItemStack? = null
  var offHand: ItemStack? = null
  var helmet: ItemStack? = null
  var chestplate: ItemStack? = null
  var leggings: ItemStack? = null
  var boots: ItemStack? = null
}

/** Represents client-side entity animation types sent via packets. */
enum class EntityAnimation {
  SWING_MAIN_HAND,
  HURT,
  WAKE_UP,
  SWING_OFF_HAND,
  CRITICAL_HIT,
  MAGIC_CRITICAL_HIT
}

/**
 * DSL builder for specifying fake block changes sent to a player.
 */
@PacketDsl
interface FakeBlockBuilder {
  fun set(location: Location, material: Material)
  fun fill(from: Location, to: Location, material: Material)
}

/**
 * DSL builder for configuring a world border packet.
 *
 * @property x The center X coordinate.
 * @property z The center Z coordinate.
 * @property size The new border size in blocks.
 * @property oldSize The previous border size (used for lerp animation).
 * @property lerpTime The transition duration in milliseconds.
 * @property warningDistance The warning distance in blocks.
 * @property warningTime The warning time in seconds.
 */
@PacketDsl
interface WorldBorderBuilder {
  var x: Double
  var z: Double
  var size: Double
  var oldSize: Double
  var lerpTime: Long
  var warningDistance: Int
  var warningTime: Int

  fun center(location: Location) {
    x = location.x
    z = location.z
  }
}

/**
 * DSL builder for constructing entity metadata entries.
 */
@PacketDsl
interface MetadataBuilder {
  fun <T> set(index: Int, type: MetadataType, value: T)
  fun setGlowing(glowing: Boolean)
  fun setCustomName(name: String?)
  fun setInvisible(invisible: Boolean)
}

/** Enumerates all supported entity metadata value types as defined by the Minecraft protocol. */
enum class MetadataType {
  BYTE,
  INT,
  FLOAT,
  STRING,
  CHAT,
  OPTCHAT,
  ITEM,
  BOOLEAN,
  ROTATION,
  POSITION,
  OPTPOSITION,
  DIRECTION,
  OPTUUID,
  BLOCKID,
  OPTBLOCKID,
  NBT,
  PARTICLE,
  VILLAGER,
  OPTINT,
  POSE,
  CAT_VARIANT,
  FROG_VARIANT,
  OPT_GLOBAL_POS,
  PAINTING_VARIANT,
  SNIFFER_STATE,
  VECTOR3,
  QUATERNION
}

/**
 * DSL builder for configuring a title packet.
 *
 * @property title The main title text.
 * @property subtitle The subtitle text.
 * @property fadeIn Fade-in duration in ticks.
 * @property stay Display duration in ticks.
 * @property fadeOut Fade-out duration in ticks.
 */
class TitleBuilder {
  var title: String = ""
  var subtitle: String = ""
  var fadeIn: Int = 10
  var stay: Int = 40
  var fadeOut: Int = 10
}

/**
 * DSL builder for configuring a sound packet.
 *
 * @property type The sound to play.
 * @property volume The volume level.
 * @property pitch The pitch level.
 * @property relative Whether the sound position is relative to the player.
 * @property offset The positional offset of the sound.
 */
class SoundBuilder {
  var type: Sound? = null
  var volume: Float = 1f
  var pitch: Float = 1f
  var relative: Boolean = false
  var offset: Vector = Vector(0, 0, 0)
}

/**
 * Runs a tick-based animation loop for this player for [duration] ticks,
 * calling [action] with the current tick index on each tick.
 *
 * @param plugin The owning plugin.
 * @param duration The total number of ticks to run.
 * @param action Called each tick with the current tick index (0-based).
 */
fun Player.packetAnimation(plugin: Plugin, duration: Int, action: (Int) -> Unit) {
  var tick = 0
  val scheduler = PluginRegistory.get(plugin)
  val handle = scheduler.timer(0L.ticks, 1L.ticks) {
    if (tick >= duration) {
      return@timer
    }
    action(tick)
    tick++
  }
  PluginRegistory.get(plugin).manager.trackPlayer(this, handle)
}

/**
 * Entry point for the packet DSL. Applies [action] to a [PacketBuilder] for the given [player].
 *
 * @param player The target player.
 * @param action DSL block for sending packet-based effects.
 */
fun packet(player: Player, action: PacketBuilder.() -> Unit) {
  PacketBuilder(player).apply(action)
}

/**
 * Creates and configures a [PacketListener] using the given DSL block.
 *
 * @param listener DSL block for registering packet handlers and transformers.
 */
fun packetListener(listener: PacketListener.() -> Unit) {
  PacketAPI.createPacketListener().apply(listener)
}

/**
 * Subscribe to specific packet events for a player.
 */
inline fun <reified T> Player.onPacket(crossinline handler: PacketEvent.(T) -> Unit) {
  Packets.onPacket(this) {
    if (packet is T) {
      handler(packet as T)
    }
  }
}

/**
 * Subscribe to specific packet events for a player asynchronously.
 */
inline fun <reified T> Player.onPacketAsync(crossinline handler: suspend PacketEvent.(T) -> Unit) {
  Packets.onPacketAsync(this, handler)
}

/**
 * Configures simulated network conditions for a player's packet pipeline.
 *
 * @property latency Artificial latency to add in milliseconds.
 * @property packetLoss Probability of dropping a packet (0.0–1.0).
 */
@PacketDsl
interface NetworkSettings {
  var latency: Long
  var packetLoss: Double
}

/**
 * Configures the simulated [NetworkSettings] for this player.
 *
 * @param action DSL block for setting latency and packet-loss values.
 */
fun Player.networkSettings(action: NetworkSettings.() -> Unit) {
  PacketAPI.getNetworkSettings(this).apply(action)
}

/**
 * Sends a raw plugin-channel packet to this player.
 *
 * @param channel The plugin channel identifier.
 * @param action DSL block for writing data into the [ByteBuf].
 */
fun Player.sendRawPacket(channel: String, action: ByteBuf.() -> Unit) {
  val buf = Unpooled.buffer()
  buf.action()
  PacketAPI.sendRawPacket(this, channel, buf)
}

/** Builder interface for configuring a generic particle/effect. */
interface EffectBuilder {
  var particle: Particle
  var location: Location?
  var data: Any?
}

/** Builder interface for configuring a scoreboard team sent via packets. */
interface TeamBuilder {
  var name: String
  var prefix: String?
  var suffix: String?
  var friendlyFire: Boolean
  var canSeeFriendly: Boolean
  var color: TextColor?
}
