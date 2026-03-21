package com.peco2282.devcore.packet

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
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.util.UUID

@DslMarker
annotation class PacketDsl

@PacketDsl
class PacketBuilder(private val player: Player) {

  fun title(builder: TitleBuilder.() -> Unit) {
    val titleBuilder = TitleBuilder().apply(builder)
    PacketAPI.sendTitle(
      player,
      titleBuilder.title,
      titleBuilder.subtitle,
      titleBuilder.fadeIn,
      titleBuilder.stay,
      titleBuilder.fadeOut
    )
  }

  fun actionBar(message: String) {
    PacketAPI.sendActionBar(player, message)
  }

  fun sound(builder: SoundBuilder.() -> Unit) {
    val soundBuilder = SoundBuilder().apply(builder)
    val sound = soundBuilder.type ?: return
    PacketAPI.sendSound(
      player,
      sound,
      soundBuilder.volume,
      soundBuilder.pitch,
      soundBuilder.relative,
      soundBuilder.offset
    )
  }

  fun sendFakeEntity(type: EntityType, location: Location, builder: FakeEntityBuilder.() -> Unit) {
    val fakeEntityBuilder = PacketAPI.createFakeEntityBuilder(player, type, location).apply(builder)
    fakeEntityBuilder.spawn()
  }

  fun particles(type: Particle, builder: ParticleBuilder.() -> Unit) {
    val particleBuilder = ParticleBuilder(type).apply(builder)
    PacketAPI.sendParticles(
      player,
      particleBuilder.type,
      particleBuilder.location ?: player.location,
      particleBuilder.amount,
      particleBuilder.offset,
      particleBuilder.extra,
      particleBuilder.data
    )
  }

  fun fakeBlocks(builder: FakeBlockBuilder.() -> Unit) {
    PacketAPI.sendFakeBlocks(player, builder)
  }

  fun camera(entityId: Int) {
    PacketAPI.sendCamera(player, entityId)
  }

  fun worldBorder(builder: WorldBorderBuilder.() -> Unit) {
    PacketAPI.sendWorldBorder(player, builder)
  }

  fun openSign(location: Location, front: Boolean = true) {
    PacketAPI.sendOpenSign(player, location, front)
  }

  fun metadata(entityId: Int, builder: MetadataBuilder.() -> Unit) {
    PacketAPI.sendMetadata(player, entityId, builder)
  }
}

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

@PacketDsl
class ParticleBuilder(val type: Particle) {
  var amount: Int = 10
  var offset: Vector = Vector(0.5, 0.5, 0.5)
  var extra: Double = 0.1
  var location: Location? = null
  var data: Any? = null
}

@PacketDsl
class EquipmentBuilder {
  var mainHand: ItemStack? = null
  var offHand: ItemStack? = null
  var helmet: ItemStack? = null
  var chestplate: ItemStack? = null
  var leggings: ItemStack? = null
  var boots: ItemStack? = null
}

enum class EntityAnimation {
  SWING_MAIN_HAND,
  HURT,
  WAKE_UP,
  SWING_OFF_HAND,
  CRITICAL_HIT,
  MAGIC_CRITICAL_HIT
}

@PacketDsl
interface FakeBlockBuilder {
  fun set(location: Location, material: Material)
  fun fill(from: Location, to: Location, material: Material)
}

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

@PacketDsl
interface MetadataBuilder {
  fun <T> set(index: Int, type: MetadataType, value: T)
  fun setGlowing(glowing: Boolean)
  fun setCustomName(name: String?)
  fun setInvisible(invisible: Boolean)
}

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

class TitleBuilder {
  var title: String = ""
  var subtitle: String = ""
  var fadeIn: Int = 10
  var stay: Int = 40
  var fadeOut: Int = 10
}

class SoundBuilder {
  var type: Sound? = null
  var volume: Float = 1f
  var pitch: Float = 1f
  var relative: Boolean = false
  var offset: Vector = Vector(0, 0, 0)
}

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

fun packet(player: Player, action: PacketBuilder.() -> Unit) {
  PacketBuilder(player).apply(action)
}

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

@PacketDsl
interface NetworkSettings {
  var latency: Long
  var packetLoss: Double
}

fun Player.networkSettings(action: NetworkSettings.() -> Unit) {
  PacketAPI.getNetworkSettings(this).apply(action)
}

fun Player.sendRawPacket(channel: String, action: ByteBuf.() -> Unit) {
  val buf = Unpooled.buffer()
  buf.action()
  PacketAPI.sendRawPacket(this, channel, buf)
}

interface EffectBuilder {
  var particle: Particle
  var location: Location?
  var data: Any?
}

interface TeamBuilder {
  var name: String
  var prefix: String?
  var suffix: String?
  var friendlyFire: Boolean
  var canSeeFriendly: Boolean
  var color: TextColor?
}
