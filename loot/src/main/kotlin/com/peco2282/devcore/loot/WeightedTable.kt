package com.peco2282.devcore.loot

import kotlin.random.Random

/** Immutable weighted selector whose weights need not add up to any fixed total. */
class WeightedTable<T> internal constructor(entries: List<Pair<T, Int>>) {
  private val entries = entries.toList()
  private val totalWeight: Int

  init {
    require(this.entries.isNotEmpty()) { "At least one weighted value is required" }
    require(this.entries.all { it.second > 0 }) { "Weights must be positive" }
    totalWeight = this.entries.sumOf { it.second }
    require(totalWeight > 0) { "Total weight overflowed" }
  }

  /** Picks one value using [random]. */
  fun pick(random: Random = Random.Default): T {
    var cursor = random.nextInt(totalWeight)
    entries.forEach { (value, weight) ->
      if (cursor < weight) return value
      cursor -= weight
    }
    error("Weighted selection exhausted unexpectedly")
  }
}

/** Creates a reusable weighted selector. */
fun <T> weighted(vararg entries: Pair<T, Int>): WeightedTable<T> = WeightedTable(entries.toList())
