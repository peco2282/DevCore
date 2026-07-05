package com.peco2282.devcore.world

import org.bukkit.World

/**
 * Sets the time of the world using a DSL-style edit block.
 *
 * This is a convenient extension function that wraps the world edit DSL
 * to set the world time.
 *
 * @param time The time to set, in ticks (0-24000 where 0 is sunrise, 6000 is noon, 12000 is sunset, 18000 is midnight).
 *
 * Example usage:
 * ```kotlin
 * world.time(6000L) // Set time to noon
 * ```
 *
 * Equivalent to:
 * ```kotlin
 * world.edit {
 *   this.time = 6000L
 * }
 * ```
 */
fun World.time(time: Long) {
  edit { this.time = time }
}

/**
 * Sets the weather of the world using a DSL-style edit block.
 *
 * This is a convenient extension function that wraps the world edit DSL
 * to set the weather type.
 *
 * @param type The weather type to set (e.g., WeatherType.CLEAR, WeatherType.RAIN, WeatherType.THUNDER).
 *
 * Example usage:
 * ```kotlin
 * world.weather(WeatherType.RAIN) // Set weather to rain
 * ```
 *
 * Equivalent to:
 * ```kotlin
 * world.edit {
 *   weather = WeatherType.RAIN
 * }
 * ```
 */
fun World.weather(type: WeatherType) {
  edit { weather = type }
}
