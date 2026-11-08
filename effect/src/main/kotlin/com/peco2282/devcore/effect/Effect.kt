package com.peco2282.devcore.effect

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player

@DslMarker
annotation class EffectDsl

/**
 * A reusable visual/audio effect that can be played at any [Location].
 *
 * Build one with [effect] or [animatedEffect], keep it in a field, and play it as often as needed.
 */
fun interface Effect {
  /**
   * Plays this effect once.
   *
   * @param location Where to play it.
   * @param audience Players who should see/hear it, or `null` for everyone near the location.
   * @param frame Frame counter. Always 0 for single plays; increases during repeated playback.
   */
  fun play(location: Location, audience: Collection<Player>?, frame: Int)
}

/** Plays this effect once at this location for everyone nearby. */
fun Effect.play(location: Location, audience: Collection<Player>? = null) = play(location, audience, 0)

/** Plays [effect] once at this location. */
fun Location.play(effect: Effect, audience: Collection<Player>? = null) = effect.play(this, audience, 0)

/** One particle/sound action inside an [Effect]. */
private fun interface Step {
  fun play(origin: Location, audience: Collection<Player>?)
}

/** DSL receiver for [effect] and [animatedEffect]. */
@EffectDsl
class EffectBuilder internal constructor() {
  private val steps = mutableListOf<Step>()

  /**
   * Spawns [particle] at every point of [shape].
   *
   * @param count Particles per point. With 0, [offset] is treated as a direction (useful for
   *   particles with velocity such as `FLAME`).
   * @param offset Random spread on each axis.
   * @param extra Particle-specific value, usually speed.
   * @param data Extra particle data, e.g. [Particle.DustOptions] for `DUST`. See [dust].
   */
  fun particle(
    particle: Particle,
    shape: Shape = point(),
    count: Int = 1,
    offset: Double = 0.0,
    extra: Double = 0.0,
    data: Any? = null
  ) {
    val points = shape.points()
    steps += Step { origin, audience ->
      for (p in points) {
        val at = origin.clone().add(p)
        if (audience == null) {
          if (data == null) at.world.spawnParticle(particle, at, count, offset, offset, offset, extra)
          else at.world.spawnParticle(particle, at, count, offset, offset, offset, extra, data)
        } else {
          for (player in audience) {
            if (data == null) player.spawnParticle(particle, at, count, offset, offset, offset, extra)
            else player.spawnParticle(particle, at, count, offset, offset, offset, extra, data)
          }
        }
      }
    }
  }

  /** Plays [sound] at the effect's origin. */
  fun sound(
    sound: Sound,
    volume: Float = 1f,
    pitch: Float = 1f,
    category: SoundCategory = SoundCategory.MASTER
  ) {
    steps += Step { origin, audience ->
      if (audience == null) origin.world.playSound(origin, sound, category, volume, pitch)
      else audience.forEach { it.playSound(origin, sound, category, volume, pitch) }
    }
  }

  /** Plays another effect at the same origin. */
  fun include(other: Effect) {
    steps += Step { origin, audience -> other.play(origin, audience, 0) }
  }

  internal fun build(): Effect {
    val snapshot = steps.toList()
    return Effect { location, audience, _ -> snapshot.forEach { it.play(location, audience) } }
  }
}

/** Colored dust options for `Particle.DUST`. Pass the result as `data`. */
fun dust(color: Color, size: Float = 1f): Particle.DustOptions = Particle.DustOptions(color, size)

/**
 * Builds a fixed [Effect]. The shapes are computed once, so playing it repeatedly is cheap.
 *
 * ```kotlin
 * val levelUp = effect {
 *   particle(Particle.END_ROD, circle(1.5).offset(y = 1.0))
 *   sound(Sound.ENTITY_PLAYER_LEVELUP)
 * }
 * levelUp.play(player.location)
 * ```
 */
fun effect(block: EffectBuilder.() -> Unit): Effect = EffectBuilder().apply(block).build()

/**
 * Builds an [Effect] that is recomputed for every frame, for motion such as spinning rings.
 * The block receives the frame number (0, 1, 2, ... while repeating).
 *
 * ```kotlin
 * val spinner = animatedEffect { frame ->
 *   particle(Particle.FLAME, circle(1.0, points = 8).rotateY(frame * 0.2))
 * }
 * plugin.playEffect(spinner, player, every = 1.ticks, duration = 5.seconds)
 * ```
 */
fun animatedEffect(block: EffectBuilder.(frame: Int) -> Unit): Effect =
  Effect { location, audience, frame -> EffectBuilder().apply { block(frame) }.build().play(location, audience, frame) }
