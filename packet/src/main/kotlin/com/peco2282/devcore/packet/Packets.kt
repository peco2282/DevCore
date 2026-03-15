package com.peco2282.devcore.packet

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
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
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Send a packet to the player.
 */
fun Player.sendPacket(packet: Any) {
  InternalAPI.sendPacket(this, packet)
}

/**
 * Event for packet intercepting.
 */
class PacketEvent(
  val player: Player,
  val packet: Any,
  var isCancelled: Boolean = false
) {
  fun cancel() {
    isCancelled = true
  }
}

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
    InternalAPI.init(plugin)
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
    InternalAPI.injectPlayer(player)
  }

  class NetworkSettingsImpl : NetworkSettings {
    override var latency: Long = 0L
    override var packetLoss: Double = 0.0
  }

  class PacketHandler(val player: Player) : ChannelDuplexHandler() {
    val settings = NetworkSettingsImpl()
    val transformersSend = mutableListOf<(Any) -> Any>()
    val transformersReceive = mutableListOf<(Any) -> Any>()

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      if (settings.packetLoss > 0.0 && Random.nextDouble() < settings.packetLoss) {
        return // Simulate packet loss
      }

      globalReadHandlers.forEach { it(ctx, msg) }

      var currentMsg = msg
      globalTransformersReceive.forEach { currentMsg = it(currentMsg) }
      transformersReceive.forEach { currentMsg = it(currentMsg) }

      val event = PacketEvent(player, currentMsg)
      packetHandlers[player.uniqueId]?.forEach { it(event) }
      globalPacketHandlers.forEach { it(event) }
      globalReceiveHandlers.forEach { it(event) }

      if (!event.isCancelled) {
        if (settings.latency > 0) {
          ctx.executor().schedule({
            super.channelRead(ctx, event.packet)
          }, settings.latency, TimeUnit.MILLISECONDS)
        } else {
          super.channelRead(ctx, event.packet)
        }
      }
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
      if (settings.packetLoss > 0.0 && Random.nextDouble() < settings.packetLoss) {
        promise.setSuccess()
        return // Simulate packet loss
      }

      globalWriteHandlers.forEach { it(ctx, msg) }

      var currentMsg = msg
      globalTransformersSend.forEach { currentMsg = it(currentMsg) }
      transformersSend.forEach { currentMsg = it(currentMsg) }

      val event = PacketEvent(player, currentMsg)
      packetHandlers[player.uniqueId]?.forEach { it(event) }
      globalPacketHandlers.forEach { it(event) }
      globalSendHandlers.forEach { it(event) }

      if (!event.isCancelled) {
        if (settings.latency > 0) {
          ctx.executor().schedule({
            super.write(ctx, event.packet, promise)
          }, settings.latency, TimeUnit.MILLISECONDS)
        } else {
          super.write(ctx, event.packet, promise)
        }
      } else {
        promise.setSuccess()
      }
    }
  }

  fun getNetworkSettings(player: Player): NetworkSettings {
    val channel = (player as CraftPlayer).handle.connection.connection.channel
    val handler = channel.pipeline().get(HANDLER_NAME) as? PacketHandler
    return handler?.settings ?: NetworkSettingsImpl()
  }

  fun createPacketListener(): PacketListener = PacketListenerImpl()

  private class PacketListenerImpl : PacketListener {
    override fun onRead(handler: (ctx: ChannelHandlerContext, msg: Any) -> Unit) {
      globalReadHandlers.add(handler)
    }

    override fun onWrite(handler: (ctx: ChannelHandlerContext, msg: Any) -> Unit) {
      globalWriteHandlers.add(handler)
    }

    override fun <T : Any> on(type: Class<T>, action: T.(PacketEvent) -> Unit) {
      globalPacketHandlers.add {
        if (type.isInstance(packet)) {
          action(packet as T, this)
        }
      }
    }

    override fun <T : Any> onSend(type: Class<T>, action: T.(PacketEvent) -> Unit) {
      globalSendHandlers.add {
        if (type.isInstance(packet)) {
          action(packet as T, this)
        }
      }
    }

    override fun <T : Any> onReceive(type: Class<T>, action: T.(PacketEvent) -> Unit) {
      globalReceiveHandlers.add {
        if (type.isInstance(packet)) {
          action(packet as T, this)
        }
      }
    }

    override fun <T : Any> transformSend(transformer: (T) -> T) {
      @Suppress("UNCHECKED_CAST")
      globalTransformersSend.add { msg ->
        try {
          // 型チェックをより安全に行うための試行
          // reified T が使えないため、ClassCastException をキャッチするか、
          // 呼び出し側から KClass を渡してもらう必要があるが、
          // 現状のDSLを維持するためにはこのアプローチをとる
          transformer(msg as T)
        } catch (e: Exception) {
          msg
        }
      }
    }

    override fun <T : Any> transformReceive(transformer: (T) -> T) {
      @Suppress("UNCHECKED_CAST")
      globalTransformersReceive.add { msg ->
        try {
          transformer(msg as T)
        } catch (e: Exception) {
          msg
        }
      }
    }
  }

  private val globalTransformersSend = mutableListOf<(Any) -> Any>()
  private val globalTransformersReceive = mutableListOf<(Any) -> Any>()
  private val globalReadHandlers = mutableListOf<(ChannelHandlerContext, Any) -> Unit>()
  private val globalWriteHandlers = mutableListOf<(ChannelHandlerContext, Any) -> Unit>()
  private val globalPacketHandlers = mutableListOf<PacketEvent.() -> Unit>()
  private val globalSendHandlers = mutableListOf<PacketEvent.() -> Unit>()
  private val globalReceiveHandlers = mutableListOf<PacketEvent.() -> Unit>()

  fun removePlayer(player: Player) {
    InternalAPI.removePlayer(player)
  }

  /**
   * Subscribe to packet events for a player.
   */
  fun onPacket(player: Player, handler: PacketEvent.() -> Unit) {
    packetHandlers.getOrPut(player.uniqueId) { mutableListOf() }.add(handler)
  }

  /**
   * Subscribe to specific packet events for a player asynchronously.
   */
  inline fun <reified T> onPacketAsync(player: Player, crossinline handler: suspend PacketEvent.(T) -> Unit) {
    onPacket(player) {
      if (packet is T) {
        val dispatcher =
          (player as CraftPlayer).handle.connection.connection.channel.eventLoop().asCoroutineDispatcher()
        CoroutineScope(dispatcher).launch {
          handler(packet as T)
        }
      }
    }
  }
}
