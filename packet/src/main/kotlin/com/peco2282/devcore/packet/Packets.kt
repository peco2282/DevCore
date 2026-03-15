package com.peco2282.devcore.packet

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.protocol.Packet
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Send a packet to the player.
 */
fun Player.sendPacket(packet: Packet<*>) {
  (this as CraftPlayer).handle.connection.send(packet)
}

/**
 * Event for packet intercepting.
 */
data class PacketEvent(
  val player: Player,
  val packet: Any,
  var isCancelled: Boolean = false
)

/**
 * DSL for building packets and handling packet events.
 */
object Packets : Listener {
  private const val HANDLER_NAME = "devcore_packet_handler"
  private val packetHandlers = ConcurrentHashMap<UUID, MutableList<PacketEvent.() -> Unit>>()
  private var isInitialized = false

  /**
   * Initialize the packet listener.
   */
  fun init(plugin: Plugin) {
    if (isInitialized) return
    isInitialized = true
    Bukkit.getPluginManager().registerEvents(this, plugin)
    Bukkit.getOnlinePlayers().forEach { injectPlayer(it) }
  }

  @EventHandler
  fun onJoin(event: PlayerJoinEvent) {
    injectPlayer(event.player)
  }

  @EventHandler
  fun onQuit(event: PlayerQuitEvent) {
    removePlayer(event.player)
    packetHandlers.remove(event.player.uniqueId)
  }

  private fun injectPlayer(player: Player) {
    val craftPlayer = player as CraftPlayer
    val connection = try {
      craftPlayer.handle.connection
    } catch (e: Exception) {
      return
    }
    if (connection == null) return

    val channel = connection.connection.channel
    if (channel.pipeline().get(HANDLER_NAME) != null) return

    channel.pipeline().addBefore("packet_handler", HANDLER_NAME, object : ChannelDuplexHandler() {
      override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val event = PacketEvent(player, msg)
        packetHandlers[player.uniqueId]?.forEach { it(event) }
        if (!event.isCancelled) {
          super.channelRead(ctx, event.packet)
        }
      }

      override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val event = PacketEvent(player, msg)
        packetHandlers[player.uniqueId]?.forEach { it(event) }
        if (!event.isCancelled) {
          super.write(ctx, event.packet, promise)
        }
      }
    })
  }

  private fun removePlayer(player: Player) {
    val craftPlayer = player as CraftPlayer
    val channel = craftPlayer.handle.connection.connection.channel
    channel.eventLoop().submit {
      if (channel.pipeline().get(HANDLER_NAME) != null) {
        channel.pipeline().remove(HANDLER_NAME)
      }
    }
  }

  /**
   * Subscribe to packet events for a player.
   */
  fun onPacket(player: Player, handler: PacketEvent.() -> Unit) {
    packetHandlers.getOrPut(player.uniqueId) { mutableListOf() }.add(handler)
  }

  /**
   * Subscribe to specific packet events for a player.
   */
  inline fun <reified T> onPacket(player: Player, crossinline handler: PacketEvent.(T) -> Unit) {
    onPacket(player) {
      if (packet is T) {
        handler(packet as T)
      }
    }
  }
}
