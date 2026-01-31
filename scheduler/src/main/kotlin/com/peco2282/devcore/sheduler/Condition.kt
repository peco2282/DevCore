package com.peco2282.devcore.sheduler

fun interface Condition {
  fun canRun(): Boolean

  fun not(): Condition = Condition { !canRun() }
  infix fun and(other: Condition): Condition = Condition { canRun() && other.canRun() }
  infix fun or(other: Condition): Condition = Condition { canRun() || other.canRun() }
  infix fun xor(other: Condition): Condition = Condition { canRun() xor other.canRun() }

  companion object {
    val ALWAYS = Condition { true }
    val NEVER = Condition { false }
  }
}
