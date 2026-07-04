@file:OptIn(ExperimentalContracts::class)

package com.peco2282.devcore.tasksequence

import com.peco2282.devcore.packet.PacketBuilder
import com.peco2282.devcore.packet.packet
import com.peco2282.devcore.scheduler.Scheduler
import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.coroutines.delayTicks
import com.peco2282.devcore.scheduler.coroutines.dispatcher
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.taskManager
import com.peco2282.devcore.scheduler.ticks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.time.Duration
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds


@DslMarker
annotation class TaskSequenceDsl

/**
 * TaskSequenceBuilder provides a DSL for creating a sequence of tasks with delays.
 *
 * @property plugin The plugin associated with this sequence.
 * @property player The player associated with this sequence, if any.
 */
@TaskSequenceDsl
class TaskSequenceBuilder(val plugin: Plugin, val player: Player? = null) {

  /**
   * Suspends the sequence for the specified number of [ticks].
   *
   * @param ticks The number of ticks to wait.
   */
  suspend fun wait(ticks: Ticks) {
    if (ticks.value <= 0) return
    try {
      delayTicks(plugin, ticks)
    } catch (e: Exception) {
      // For testing or when Bukkit is not available
    }
  }

  /**
   * Suspends the sequence for the specified number of ticks.
   *
   * @param ticks The number of ticks to wait.
   */
  suspend fun wait(ticks: Long): Unit = wait(Ticks(ticks))

  /**
   * Suspends the sequence for the specified number of ticks.
   *
   * @param ticks The number of ticks to wait.
   */
  suspend fun wait(ticks: Int): Unit = wait(Ticks(ticks.toLong()))

  /**
   * Suspends the sequence for the specified [duration].
   *
   * @param duration The duration to wait.
   */
  suspend fun wait(duration: Duration): Unit = wait((duration.toMillis() / 50).ticks)

  /**
   * Suspends the sequence for 1 tick.
   */
  suspend fun yield(): Unit = wait(1)

  /**
   * Suspends the sequence until the [condition] is met.
   *
   * @param checkInterval The interval between condition checks.
   * @param condition The condition to check.
   */
  suspend fun waitUntil(checkInterval: Ticks = 1.ticks, condition: () -> Boolean) {
    while (!condition()) {
      wait(checkInterval)
    }
  }

  /**
   * Runs the [block] on the Bukkit main thread.
   *
   * @param block The block to execute.
   */
  suspend fun <T> sync(block: suspend CoroutineScope.() -> T): T {
    contract {
      callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return withContext(plugin.dispatcher) {
      block()
    }
  }

  /**
   * Runs the [block] asynchronously.
   *
   * @param block The block to execute.
   */
  suspend fun <T> async(block: suspend CoroutineScope.() -> T): T {
    contract { 
      callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return withContext(Dispatchers.Default) {
      block()
    }
  }

  /**
   * Repeats the [block] [times] times.
   *
   * @param times The number of times to repeat.
   * @param block The block to execute, receiving the current iteration index.
   */
  suspend fun repeat(times: Int, block: suspend TaskSequenceBuilder.(Int) -> Unit) {
    for (i in 0 until times) {
      block(i)
    }
  }

  /**
   * Repeats the [block] while [condition] is true.
   *
   * @param condition The condition to check.
   * @param block The block to execute.
   */
  suspend fun repeatWhile(condition: () -> Boolean, block: suspend TaskSequenceBuilder.() -> Unit) {
    while (condition()) {
      block()
    }
  }

  /**
   * Repeats the [block] until [condition] is true.
   *
   * @param condition The condition to check.
   * @param block The block to execute.
   */
  suspend fun repeatUntil(condition: () -> Boolean, block: suspend TaskSequenceBuilder.() -> Unit) {
    while (!condition()) {
      block()
    }
  }

  /**
   * Runs the [block] if the [condition] is true.
   *
   * @param condition The condition to check.
   * @param block The block to execute.
   */
  suspend fun runIf(condition: () -> Boolean, block: suspend TaskSequenceBuilder.() -> Unit) {
    if (condition()) {
      block()
    }
  }

  /**
   * Runs the [block] if the [condition] is false.
   *
   * @param condition The condition to check.
   * @param block The block to execute.
   */
  suspend fun runUnless(condition: () -> Boolean, block: suspend TaskSequenceBuilder.() -> Unit) {
    if (!condition()) {
      block()
    }
  }

  /**
   * Repeats the [block] [times] times with a [delay] between each iteration.
   *
   * @param times The number of times to repeat.
   * @param delay The delay between each iteration.
   * @param block The block to execute.
   */
  suspend fun repeatEvery(times: Int, delay: Ticks, block: suspend TaskSequenceBuilder.(Int) -> Unit) {
    for (i in 0 until times) {
      block(i)
      if (i < times - 1) {
        wait(delay)
      }
    }
  }

  /**
   * Runs the [block] with a timeout.
   * If the block takes longer than [ticks], it will be cancelled.
   *
   * @param ticks The timeout duration in ticks.
   * @param block The block to execute.
   * @return The result of the block, or null if it timed out.
   */
  suspend fun <T> timeout(ticks: Ticks, block: suspend TaskSequenceBuilder.() -> T): T? {
    contract {
      callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return try {
      withTimeout((ticks.value * 50).milliseconds) {
        block()
      }
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Sends a message to the player if they are online.
   *
   * @param message The message to send.
   */
  fun sendMessage(message: Component) {
    player?.sendMessage(message)
  }

  /**
   * Sends an action bar message to the player if they are online.
   *
   * @param message The message to send.
   */
  fun sendActionBar(message: Component) {
    player?.sendActionBar(message)
  }

  /**
   * Performs an action using the Packet DSL for the player.
   * If the player is not specified, this does nothing.
   *
   * @param action The Packet DSL block.
   */
  fun packet(action: PacketBuilder.() -> Unit) {
    player?.packet(action)
  }
}

/**
 * Starts a task sequence.
 *
 * @param context Additional coroutine context.
 * @param block The sequence block.
 */
fun Plugin.sequence(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend TaskSequenceBuilder.() -> Unit
) {
  scheduler.launch(context) {
    val builder = TaskSequenceBuilder(this@sequence)
    block(builder)
  }
}

/**
 * Starts a task sequence using this [Scheduler].
 *
 * @param context Additional coroutine context.
 * @param block The sequence block.
 */
fun Scheduler.sequence(
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend TaskSequenceBuilder.() -> Unit
) {
  val p = this::class.java.getDeclaredField("plugin").apply { isAccessible = true }.get(this) as Plugin
  launch(context) {
    val builder = TaskSequenceBuilder(p)
    block(builder)
  }
}

/**
 * Extension for Player to start a task sequence.
 * The sequence will be tracked for this player.
 *
 * @param plugin The plugin associated with this sequence.
 * @param context Additional coroutine context.
 * @param block The sequence block.
 */
fun Player.sequence(
  plugin: Plugin,
  context: CoroutineContext = EmptyCoroutineContext,
  block: suspend TaskSequenceBuilder.() -> Unit
) {
  val handle = plugin.scheduler.launch(context) {
    val builder = TaskSequenceBuilder(plugin, this@sequence)
    block(builder)
  }
  plugin.taskManager.trackPlayer(this, handle)
}
