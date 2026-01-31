package com.peco2282.devcore.sheduler

@JvmInline
value class Ticks(val value: Long)

val Int.ticks get() = Ticks(this.toLong())
val Long.ticks get() = Ticks(this)

val Int.seconds get() = Ticks(this * 20L)
val Int.minutes get() = Ticks(this * 20L * 60)
