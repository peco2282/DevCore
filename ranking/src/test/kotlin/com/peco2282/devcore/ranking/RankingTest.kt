package com.peco2282.devcore.ranking

import kotlin.test.Test
import kotlin.test.assertEquals

class RankingTest {
  @Test fun `ties use competition ranks and deterministic id order`() {
    val ranking = Ranking<String>()
    ranking.update(mapOf("b" to 10, "a" to 10, "c" to 5))
    assertEquals(listOf(1, 1, 3), ranking.all().map { it.rank })
    assertEquals(listOf("a", "b", "c"), ranking.all().map { it.id })
    assertEquals(3, ranking.rankOf("c")?.rank)
  }
}
