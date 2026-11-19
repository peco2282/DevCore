package com.peco2282.devcore.util.animation

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import kotlin.time.Duration

/** A finite sequence interpolating two numbers over [frameCount] intervals. */
class Tween internal constructor(
  val from: Double,
  val to: Double,
  val frameCount: Int,
  val easing: (Double) -> Double
) {
  init { require(frameCount > 0) { "frames must be positive" } }

  /** Samples a frame, clamping indices outside the timeline. */
  fun valueAt(frame: Int): Double {
    val progress = frame.coerceIn(0, frameCount).toDouble() / frameCount
    return from + (to - from) * easing(progress)
  }

  /** Returns all frames, including both endpoints. */
  fun frames(): Sequence<Double> = (0..frameCount).asSequence().map(::valueAt)

  /** Plays on the server thread. Bukkit automatically cancels the task when [plugin] disables. */
  fun start(plugin: Plugin, periodTicks: Long = 1L, update: (Double) -> Unit): BukkitTask {
    require(periodTicks > 0) { "periodTicks must be positive" }
    var frame = 0
    lateinit var task: BukkitTask
    task = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
      update(valueAt(frame))
      if (frame++ >= frameCount) task.cancel()
    }, 0L, periodTicks)
    return task
  }
}

/** Creates a frame-count based tween. */
fun tween(from: Double, to: Double, frames: Int, easing: (Double) -> Double = Easing.LINEAR::invoke): Tween =
  Tween(from, to, frames, easing)

/** Creates a 20 TPS tween from a duration. */
fun tween(from: Double, to: Double, duration: Duration, easing: (Double) -> Double = Easing.LINEAR::invoke): Tween {
  require(duration.isPositive()) { "duration must be positive" }
  return tween(from, to, (duration.inWholeMilliseconds / 50L).coerceAtLeast(1L).toInt(), easing)
}
