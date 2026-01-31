package com.peco2282.devcore.cooldown

fun interface TimeSource {
  fun nowMillis(): Long
}

object SystemTimeSource : TimeSource {
  override fun nowMillis(): Long = System.currentTimeMillis()
}

