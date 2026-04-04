package com.peco2282.devcore.packet

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
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
 * Sends a raw NMS packet to the player via [PacketAPI].
 *
 * @param packet The NMS packet instance.
 */
fun Player.sendPacket(packet: Any) {
  PacketAPI.sendPacket(this, packet)
}

/**
 * Represents a packet event that can be inspected or cancelled during interception.
 *
 * @property player The player associated with this packet.
 * @property packet The intercepted packet object.
 * @property isCancelled Whether the packet has been cancelled and should not be forwarded.
 */
class PacketEvent(
  val player: Player,
  val packet: Any,
  var isCancelled: Boolean = false
) {
  /** Cancels this packet, preventing it from being forwarded through the pipeline. */
  fun cancel() {
    isCancelled = true
  }
}

/**
 * Central singleton for managing packet interception, player injection, and global packet handlers.
 *
 * Must be initialized via [init] before use. Automatically injects and removes players
 * on join/quit events.
 */
object Packets : Listener {
  private const val HANDLER_NAME = "devcore_packet_handler"
  private val packetHandlers = ConcurrentHashMap<UUID, MutableList<PacketEvent.() -> Unit>>()
  private var isInitialized = false

  /**
   * Initializes the packet system, registers Bukkit events, and injects all online players.
   *
   * Must be called once during plugin startup. Subsequent calls are no-ops.
   *
   * @param plugin The owning plugin instance.
   */
  fun init(plugin: Plugin) {
    if (isInitialized) return
    isInitialized = true
    PacketAPI.init(plugin)
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
    PacketAPI.injectPlayer(player)
  }

  /** Default [NetworkSettings] implementation used when no version-specific one is available. */
  class NetworkSettingsImpl : NetworkSettings {
    override var latency: Long = 0L
    override var packetLoss: Double = 0.0
  }

  /**
   * Netty [ChannelDuplexHandler] that intercepts inbound and outbound packets for a player.
   *
   * Applies transformers, fires [PacketEvent] handlers, and supports latency/packet-loss simulation.
   *
   * @property player The player this handler is attached to.
   */
  class PacketHandler(val player: Player) : ChannelDuplexHandler() {
    val settings = NetworkSettingsImpl()
    var logPackets: Boolean = false
    val transformersSend = mutableListOf<(Any) -> Any>()
    val transformersReceive = mutableListOf<(Any) -> Any>()

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
      if (logPackets || globalLogPackets) {
        Bukkit.getConsoleSender().sendMessage("§7[PACKET] §9IN: §f${msg::class.java.simpleName} §8($player)")
      }
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
      if (logPackets || globalLogPackets) {
        Bukkit.getConsoleSender().sendMessage("§7[PACKET] §aOUT: §f${msg::class.java.simpleName} §8($player)")
      }
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
    return PacketAPI.getNetworkSettings(player)
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
          // Attempt to perform type check safely.
          // Since reified T cannot be used here, we catch ClassCastException
          // or would need to pass KClass from the caller side.
          // We take this approach to maintain the current DSL.
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

    override fun logPackets(enabled: Boolean) {
      globalLogPackets = enabled
    }

    override fun measureLatency(enabled: Boolean) {
      globalMeasureLatency = enabled
    }
  }

  private var globalLogPackets: Boolean = false
  private var globalMeasureLatency: Boolean = false
  private val globalTransformersSend = mutableListOf<(Any) -> Any>()
  private val globalTransformersReceive = mutableListOf<(Any) -> Any>()
  private val globalReadHandlers = mutableListOf<(ChannelHandlerContext, Any) -> Unit>()
  private val globalWriteHandlers = mutableListOf<(ChannelHandlerContext, Any) -> Unit>()
  private val globalPacketHandlers = mutableListOf<PacketEvent.() -> Unit>()
  private val globalSendHandlers = mutableListOf<PacketEvent.() -> Unit>()
  private val globalReceiveHandlers = mutableListOf<PacketEvent.() -> Unit>()

  fun removePlayer(player: Player) {
    PacketAPI.removePlayer(player)
  }

  /**
   * Subscribes to all packet events (both sent and received) for the given player.
   *
   * @param player The player to observe.
   * @param handler Called for each packet event with the [PacketEvent] as receiver.
   */
  fun onPacket(player: Player, handler: PacketEvent.() -> Unit) {
    packetHandlers.getOrPut(player.uniqueId) { mutableListOf() }.add(handler)
  }

  /**
   * Subscribes to packet events of type [T] for the given player and handles them asynchronously
   * on the player's Netty event-loop dispatcher (falls back to [Dispatchers.Default]).
   *
   * @param T The packet type to listen for.
   * @param player The player to observe.
   * @param handler Suspend function called with the typed packet as argument.
   */
  inline fun <reified T> onPacketAsync(player: Player, crossinline handler: suspend PacketEvent.(T) -> Unit) {
    onPacket(player) {
      if (packet is T) {
        val dispatcher = PacketAPI.getCoroutineDispatcher(player) ?: Dispatchers.Default
        CoroutineScope(dispatcher).launch {
          handler(packet as T)
        }
      }
    }
  }
}
