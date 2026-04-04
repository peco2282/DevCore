package com.peco2282.devcore.entity

import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.time.Duration

/**
 * Applies a potion effect to this living entity with the specified parameters.
 *
 * The [duration] is automatically converted from Kotlin [Duration] to ticks (1 tick = 50ms).
 *
 * @param type the type of potion effect to apply
 * @param duration the duration of the effect
 * @param amplifier the amplifier (level - 1) of the effect; defaults to 0
 * @param ambient whether the effect is ambient (produces fewer particles); defaults to true
 * @param particles whether to show particles; defaults to true
 * @param icon whether to show the effect icon in the HUD; defaults to true
 */
fun LivingEntity.addEffect(
  type: PotionEffectType,
  duration: Duration,
  amplifier: Int = 0,
  ambient: Boolean = true,
  particles: Boolean = true,
  icon: Boolean = true
) {
  this.addPotionEffect(
    PotionEffect(
      type,
      (duration.inWholeMilliseconds / 50).toInt(),
      amplifier,
      ambient,
      particles,
      icon
    )
  )
}

/** Applies the [Speed][PotionEffectType.SPEED] effect to this entity. */
fun LivingEntity.speed(duration: Duration, amplifier: Int = 0) = addEffect(PotionEffectType.SPEED, duration, amplifier)

/** Applies the [Haste][PotionEffectType.HASTE] effect to this entity. */
fun LivingEntity.haste(duration: Duration, amplifier: Int = 0) = addEffect(PotionEffectType.HASTE, duration, amplifier)

/** Applies the [Strength][PotionEffectType.STRENGTH] effect to this entity. */
fun LivingEntity.strength(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.STRENGTH, duration, amplifier)

/** Applies the [Jump Boost][PotionEffectType.JUMP_BOOST] effect to this entity. */
fun LivingEntity.jump(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.JUMP_BOOST, duration, amplifier)

/** Applies the [Regeneration][PotionEffectType.REGENERATION] effect to this entity. */
fun LivingEntity.regeneration(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.REGENERATION, duration, amplifier)

/** Applies the [Resistance][PotionEffectType.RESISTANCE] effect to this entity. */
fun LivingEntity.resistance(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.RESISTANCE, duration, amplifier)

/** Applies the [Fire Resistance][PotionEffectType.FIRE_RESISTANCE] effect to this entity. */
fun LivingEntity.fireResistance(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.FIRE_RESISTANCE, duration, amplifier)

/** Applies the [Water Breathing][PotionEffectType.WATER_BREATHING] effect to this entity. */
fun LivingEntity.waterBreathing(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.WATER_BREATHING, duration, amplifier)

/** Applies the [Invisibility][PotionEffectType.INVISIBILITY] effect to this entity. */
fun LivingEntity.invisibility(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.INVISIBILITY, duration, amplifier)

/** Applies the [Blindness][PotionEffectType.BLINDNESS] effect to this entity. */
fun LivingEntity.blindness(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.BLINDNESS, duration, amplifier)

/** Applies the [Night Vision][PotionEffectType.NIGHT_VISION] effect to this entity. */
fun LivingEntity.nightVision(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.NIGHT_VISION, duration, amplifier)

/** Applies the [Hunger][PotionEffectType.HUNGER] effect to this entity. */
fun LivingEntity.hunger(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.HUNGER, duration, amplifier)

/** Applies the [Weakness][PotionEffectType.WEAKNESS] effect to this entity. */
fun LivingEntity.weakness(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.WEAKNESS, duration, amplifier)

/** Applies the [Poison][PotionEffectType.POISON] effect to this entity. */
fun LivingEntity.poison(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.POISON, duration, amplifier)

/** Applies the [Wither][PotionEffectType.WITHER] effect to this entity. */
fun LivingEntity.wither(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.WITHER, duration, amplifier)

/** Applies the [Health Boost][PotionEffectType.HEALTH_BOOST] effect to this entity. */
fun LivingEntity.healthBoost(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.HEALTH_BOOST, duration, amplifier)

/** Applies the [Absorption][PotionEffectType.ABSORPTION] effect to this entity. */
fun LivingEntity.absorption(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.ABSORPTION, duration, amplifier)

/** Applies the [Saturation][PotionEffectType.SATURATION] effect to this entity. */
fun LivingEntity.saturation(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.SATURATION, duration, amplifier)

/** Applies the [Glowing][PotionEffectType.GLOWING] effect to this entity. */
fun LivingEntity.glowing(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.GLOWING, duration, amplifier)

/** Applies the [Levitation][PotionEffectType.LEVITATION] effect to this entity. */
fun LivingEntity.levitation(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.LEVITATION, duration, amplifier)

/** Applies the [Luck][PotionEffectType.LUCK] effect to this entity. */
fun LivingEntity.luck(duration: Duration, amplifier: Int = 0) = addEffect(PotionEffectType.LUCK, duration, amplifier)

/** Applies the [Unluck][PotionEffectType.UNLUCK] effect to this entity. */
fun LivingEntity.unluck(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.UNLUCK, duration, amplifier)

/** Applies the [Slow Falling][PotionEffectType.SLOW_FALLING] effect to this entity. */
fun LivingEntity.slowFalling(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.SLOW_FALLING, duration, amplifier)

/** Applies the [Conduit Power][PotionEffectType.CONDUIT_POWER] effect to this entity. */
fun LivingEntity.conduitPower(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.CONDUIT_POWER, duration, amplifier)

/** Applies the [Dolphin's Grace][PotionEffectType.DOLPHINS_GRACE] effect to this entity. */
fun LivingEntity.dolphinsGrace(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.DOLPHINS_GRACE, duration, amplifier)

/** Applies the [Bad Omen][PotionEffectType.BAD_OMEN] effect to this entity. */
fun LivingEntity.badOmen(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.BAD_OMEN, duration, amplifier)

/** Applies the [Hero of the Village][PotionEffectType.HERO_OF_THE_VILLAGE] effect to this entity. */
fun LivingEntity.heroOfTheVillage(duration: Duration, amplifier: Int = 0) =
  addEffect(PotionEffectType.HERO_OF_THE_VILLAGE, duration, amplifier)
