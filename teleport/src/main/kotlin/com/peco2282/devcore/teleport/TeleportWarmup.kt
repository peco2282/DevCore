package com.peco2282.devcore.teleport

import com.peco2282.devcore.cooldown.PlayerCooldowns
import com.peco2282.devcore.cooldown.tryUse
import com.peco2282.devcore.effect.Effect
import com.peco2282.devcore.effect.play
import com.peco2282.devcore.scheduler.TaskHandle
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.taskManager
import com.peco2282.devcore.scheduler.ticks
import java.util.WeakHashMap
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

/** Configuration DSL for [teleportWithWarmup]. */
class TeleportWarmupBuilder internal constructor() {
  internal var movementTolerance: Double? = null
  internal var cancelOnDamage = false
  internal var tickAction: (Duration) -> Unit = {}
  internal var cancelAction: (CancelReason) -> Unit = {}

  /** Optional effect played for this player after a successful teleport. */
  var effect: Effect? = null

  /** Minimum time before the player may start another warmup. */
  var cooldown: Duration = ZERO

  /** Enables movement cancellation beyond [tolerance] blocks from the start. */
  fun cancelOnMove(tolerance: Double = 0.5) {
    require(tolerance.isFinite() && tolerance >= 0.0)
    movementTolerance = tolerance
  }

  /** Enables cancellation when the player takes damage. */
  fun cancelOnDamage() { cancelOnDamage = true }

  /** Receives the remaining duration once per server tick. */
  fun onTick(action: (Duration) -> Unit) { tickAction = action }

  /** Receives the first reason that cancels this warmup. */
  fun onCancel(action: (CancelReason) -> Unit) { cancelAction = action }
}

/** Handle for observing or explicitly cancelling an active teleport warmup. */
interface TeleportWarmupHandle {
  val status: WarmupStatus
  val cancelReason: CancelReason?
  fun cancel(reason: CancelReason = CancelReason.QUIT): Boolean
}

private class BukkitWarmupHandle(
  private val state: WarmupState,
  private val onCancel: (CancelReason) -> Unit,
) : TeleportWarmupHandle {
  var task: TaskHandle? = null
  var listener: Listener? = null
  private var failure: CancelReason? = null

  override val status: WarmupStatus get() = if (failure == null) state.status else WarmupStatus.CANCELLED
  override val cancelReason: CancelReason? get() = failure ?: state.cancelReason

  override fun cancel(reason: CancelReason): Boolean {
    if (!state.cancel(reason)) return false
    close()
    onCancel(reason)
    return true
  }

  fun close() {
    task?.cancel()
    listener?.let(HandlerList::unregisterAll)
    task = null
    listener = null
  }

  fun moved(distanceSquared: Double): Boolean {
    if (!state.moved(distanceSquared)) return false
    close()
    onCancel(CancelReason.MOVED)
    return true
  }

  fun fail(reason: CancelReason) {
    if (failure != null) return
    failure = reason
    close()
    onCancel(reason)
  }
}

private val cooldowns = WeakHashMap<Plugin, PlayerCooldowns>()

/**
 * Starts a lifecycle-bound teleport warmup for this player.
 *
 * The scheduler task and listeners are removed on completion/cancellation. Quitting always
 * cancels with [CancelReason.QUIT]. The destination is searched and loaded asynchronously at
 * completion through [teleportSafely].
 */
fun Player.teleportWithWarmup(
  plugin: Plugin,
  target: Location,
  warmup: Duration,
  block: TeleportWarmupBuilder.() -> Unit = {},
): TeleportWarmupHandle {
  require(warmup.isPositive()) { "Warmup must be positive" }
  val config = TeleportWarmupBuilder().apply(block)
  require(!config.cooldown.isNegative()) { "Cooldown must be non-negative" }
  val playerCooldowns = synchronized(cooldowns) { cooldowns.getOrPut(plugin) { PlayerCooldowns() } }
  if (config.cooldown.isPositive() && !playerCooldowns.tryUse(this, config.cooldown)) {
    config.cancelAction(CancelReason.COOLDOWN)
    return object : TeleportWarmupHandle {
      override val status = WarmupStatus.CANCELLED
      override val cancelReason = CancelReason.COOLDOWN
      override fun cancel(reason: CancelReason) = false
    }
  }

  val totalTicks = ceil(warmup.inWholeMilliseconds / 50.0).toInt().coerceAtLeast(1)
  val state = WarmupState(totalTicks, config.movementTolerance ?: Double.MAX_VALUE)
  val handle = BukkitWarmupHandle(state, config.cancelAction)
  val start = location.clone()
  val listener = object : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onMove(event: PlayerMoveEvent) {
      if (event.player.uniqueId != uniqueId || config.movementTolerance == null) return
      val to = event.to
      if (to.world != start.world) handle.cancel(CancelReason.MOVED)
      else handle.moved(start.distanceSquared(to))
    }

    @EventHandler(ignoreCancelled = true)
    fun onDamage(event: EntityDamageEvent) {
      if (config.cancelOnDamage && event.entity.uniqueId == uniqueId) handle.cancel(CancelReason.DAMAGED)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
      if (event.player.uniqueId == uniqueId) handle.cancel(CancelReason.QUIT)
    }
  }
  handle.listener = listener
  plugin.server.pluginManager.registerEvents(listener, plugin)

  handle.task = plugin.scheduler.timer(1.ticks, 1.ticks) {
    val remaining = state.tick()
    config.tickAction((remaining * 50L).milliseconds)
    if (state.status == WarmupStatus.COMPLETED) {
      handle.close()
      teleportSafely(target).whenComplete { success, error ->
        if (error == null && success == true) config.effect?.play(location, listOf(this))
        else handle.fail(CancelReason.NO_SAFE_LOCATION)
      }
    }
  }.also { plugin.taskManager.trackPlayer(this, it) }
  return handle
}
