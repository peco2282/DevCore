package com.peco2282.devcore.scheduler

/**
 * Represents a condition that can be evaluated to determine if a task should run.
 */
fun interface Condition {
  /**
   * Evaluates the condition.
   *
   * @return true if the condition is met and the task can run, false otherwise
   */
  fun canRun(): Boolean

  /**
   * Returns a condition that is the logical negation of this condition.
   *
   * @return a new [Condition] that returns true when this condition returns false
   */
  fun not(): Condition = Condition { !canRun() }

  /**
   * Returns a condition that is the logical AND of this condition and the [other] condition.
   *
   * @param other the other condition to combine with
   * @return a new [Condition] that returns true only if both conditions are met
   */
  infix fun and(other: Condition): Condition = Condition { canRun() && other.canRun() }

  /**
   * Returns a condition that is the logical OR of this condition and the [other] condition.
   *
   * @param other the other condition to combine with
   * @return a new [Condition] that returns true if either condition is met
   */
  infix fun or(other: Condition): Condition = Condition { canRun() || other.canRun() }

  /**
   * Returns a condition that is the logical XOR of this condition and the [other] condition.
   *
   * @param other the other condition to combine with
   * @return a new [Condition] that returns true if exactly one of the conditions is met
   */
  infix fun xor(other: Condition): Condition = Condition { canRun() xor other.canRun() }

  companion object {
    /**
     * A condition that is always met (always returns true).
     */
    val ALWAYS = Condition { true }

    /**
     * A condition that is never met (always returns false).
     */
    val NEVER = Condition { false }
  }
}
