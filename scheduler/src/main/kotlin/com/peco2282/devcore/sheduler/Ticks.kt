package com.peco2282.devcore.sheduler

internal const val TICKS: Long = 20.toLong()

/**
 * Represents a duration in Minecraft ticks.
 *
 * @property value the number of ticks
 */
@JvmInline
value class Ticks(val value: Long)

val ZERO = 0.ticks

/**
 * Converts this [Int] to [Ticks].
 *
 * @return a [Ticks] instance representing this integer value
 */
val Int.ticks get() = Ticks(this.toLong())

/**
 * Converts this [Long] to [Ticks].
 *
 * @return a [Ticks] instance representing this long value
 */
val Long.ticks get() = Ticks(this)

val Float.seconds get() = Ticks((this * TICKS).toLong())

val Double.seconds get() = Ticks((this * TICKS).toLong())

/**
 * Converts this [Int] seconds to [Ticks].
 *
 * Assumes 20 ticks per second.
 *
 * @return a [Ticks] instance representing the equivalent of this many seconds
 */
val Int.seconds get() = Ticks(this * TICKS)

/**
 * Converts this [Int] minutes to [Ticks].
 *
 * Assumes 20 ticks per second and 60 seconds per minute.
 *
 * @return a [Ticks] instance representing the equivalent of this many minutes
 */
val Int.minutes get() = Ticks(this * TICKS * 60)
