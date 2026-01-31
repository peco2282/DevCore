package com.peco2282.devcore.cooldown

/**
 * Represents a source of time.
 */
fun interface TimeSource {
  /**
   * Returns the current time in milliseconds.
   */
  fun nowMillis(): Long
}

/**
 * A [TimeSource] that uses the system time.
 */
object SystemTimeSource : TimeSource {
  override fun nowMillis(): Long = System.currentTimeMillis()
}

