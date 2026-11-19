package com.peco2282.devcore.ranking

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration

/** One immutable ranking row; [rank] is one-based. */
data class RankedEntry<K>(val rank: Int, val id: K, val score: Long)

/**
 * Atomically published ranking snapshots sorted by descending score.
 *
 * Readers never observe a half-built refresh. Equal scores keep the iteration order of the source
 * map. Use [top] for a leaderboard and [find] for a participant's current position.
 */
class Ranking<K : Any> internal constructor() {
  private val snapshot = AtomicReference<List<RankedEntry<K>>>(emptyList())

  fun update(scores: Map<K, Long>) {
    val sorted = scores.entries.sortedWith(compareByDescending<Map.Entry<K, Long>> { it.value }.thenBy { it.key.toString() })
    var previousScore: Long? = null
    var previousRank = 0
    snapshot.set(sorted.mapIndexed { index, entry ->
      val rank = if (entry.value == previousScore) previousRank else index + 1
      previousScore = entry.value; previousRank = rank
      RankedEntry(rank, entry.key, entry.value)
    })
  }

  fun top(limit: Int): List<RankedEntry<K>> { require(limit >= 0); return snapshot.get().take(limit) }
  fun rankOf(id: K): RankedEntry<K>? = snapshot.get().firstOrNull { it.id == id }
  fun all(): List<RankedEntry<K>> = snapshot.get()
}

class RankingHandle<K : Any> internal constructor(val ranking: Ranking<K>, private val task: BukkitTask) {
  fun refreshNow(scores: Map<K, Long>) = ranking.update(scores)
  fun close() = task.cancel()
}

/**
 * Creates a ranking and refreshes it immediately and then every [refresh].
 *
 * [source] executes on the Bukkit scheduler and must not block. Fetch remote/database values ahead
 * of time or maintain an in-memory snapshot. Cancel the returned handle when no longer required.
 */
fun <K : Any> Plugin.ranking(refresh: Duration, source: () -> Map<K, Long>): RankingHandle<K> {
  require(refresh.isPositive())
  val ranking = Ranking<K>()
  val ticks = (refresh.inWholeMilliseconds / 50).coerceAtLeast(1)
  val task = server.scheduler.runTaskTimerAsynchronously(this, Runnable { ranking.update(source()) }, 0L, ticks)
  return RankingHandle(ranking, task)
}
