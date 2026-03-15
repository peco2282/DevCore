package com.peco2282.devcore.packet

import com.peco2282.devcore.scheduler.PluginRegistory
import com.peco2282.devcore.scheduler.ticks
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

@DslMarker
annotation class PacketDsl

@PacketDsl
class PacketBuilder(private val player: Player) {

  fun title(builder: TitleBuilder.() -> Unit) {
    val titleBuilder = TitleBuilder().apply(builder)
    InternalAPI.sendTitle(
      player,
      titleBuilder.title,
      titleBuilder.subtitle,
      titleBuilder.fadeIn,
      titleBuilder.stay,
      titleBuilder.fadeOut
    )
  }

  fun actionBar(message: String) {
    InternalAPI.sendActionBar(player, message)
  }

  fun sound(builder: SoundBuilder.() -> Unit) {
    val soundBuilder = SoundBuilder().apply(builder)
    val sound = soundBuilder.type ?: return
    InternalAPI.sendSound(
      player,
      sound,
      soundBuilder.volume,
      soundBuilder.pitch,
      soundBuilder.relative,
      soundBuilder.offset
    )
  }

  fun sendFakeEntity(type: EntityType, location: Location, builder: FakeEntityBuilder.() -> Unit) {
    val fakeEntityBuilder = InternalAPI.createFakeEntityBuilder(player, type, location).apply(builder)
    fakeEntityBuilder.spawn()
  }
}

@PacketDsl
interface FakeEntityBuilder {
  var customName: String?
  var isCustomNameVisible: Boolean
  var isInvisible: Boolean
  var isGlowing: Boolean

  fun equipment(slot: EquipmentSlot, item: ItemStack)
  fun spawn()
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
  InternalAPI.createPacketListener().apply(listener)
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
  InternalAPI.getNetworkSettings(this).apply(action)
}

fun Player.sendRawPacket(channel: String, action: FriendlyByteBuf.() -> Unit) {
  val buf = FriendlyByteBuf(Unpooled.buffer())
  buf.action()
  InternalAPI.sendRawPacket(this, channel, buf)
}
