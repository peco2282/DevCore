package com.peco2282.devcore.effect

import com.peco2282.devcore.scheduler.TaskHandle
import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.ticks
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Plays [effect] repeatedly at [location()][location] until cancelled or [duration] elapses.
 *
 * The location is read on every frame, so it can follow something that moves. Returning `null`
 * from [location] stops the playback.
 *
 * @param every Time between frames.
 * @param duration Total time to play for, or `null` to play until the returned handle is cancelled
 *   (or the plugin is disabled).
 * @return A handle to cancel the playback.
 */
fun Plugin.playEffect(
  effect: Effect,
  every: Ticks = 2.ticks,
  duration: Ticks? = null,
  audience: Collection<Player>? = null,
  location: () -> Location?
): TaskHandle {
  require(every.value > 0) { "every must be positive: ${every.value}" }
  val maxFrames = duration?.let { (it.value / every.value).toInt().coerceAtLeast(1) }
  var frame = 0
  lateinit var handle: TaskHandle
  handle = scheduler.timer(0.ticks, every) {
    val at = location()
    if (at == null || (maxFrames != null && frame >= maxFrames)) {
      handle.cancel()
    } else {
      effect.play(at, audience, frame++)
    }
  }
  return handle
}

/** Plays [effect] repeatedly at a fixed [location]. See the main overload for parameters. */
fun Plugin.playEffect(
  effect: Effect,
  location: Location,
  every: Ticks = 2.ticks,
  duration: Ticks? = null,
  audience: Collection<Player>? = null
): TaskHandle = playEffect(effect, every, duration, audience) { location }

/**
 * Plays [effect] repeatedly at [entity]'s location, following it. Stops automatically
 * when the entity is removed (or the player logs out).
 */
fun Plugin.playEffect(
  effect: Effect,
  entity: Entity,
  every: Ticks = 2.ticks,
  duration: Ticks? = null,
  audience: Collection<Player>? = null
): TaskHandle = playEffect(effect, every, duration, audience) { if (entity.isValid) entity.location else null }
