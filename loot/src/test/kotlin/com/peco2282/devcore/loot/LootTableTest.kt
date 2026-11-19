package com.peco2282.devcore.loot

import org.bukkit.Material
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LootTableTest {
  @Test
  fun `weighted selection is deterministic and validates weights`() {
    val table = weighted("common" to 3, "rare" to 1)
    assertEquals(List(8) { table.pick(Random(4)) }, List(8) { table.pick(Random(4)) })
    assertFailsWith<IllegalArgumentException> { weighted("bad" to 0) }
  }

  @Test
  fun `table combines rolls guaranteed entries conditions and bonuses`() {
    val table = lootTable<String> {
      rolls = 2..2
      weight(1) { item(Material.IRON_INGOT, 2..2) }
      weight(1, condition = { it == "vip" }) { item(Material.GOLD_INGOT) }
      guaranteed { item(Material.BREAD) }
      bonus(1.0) { item(Material.DIAMOND) }
    }

    val normal = table.roll(Random(1), "normal")
    assertEquals(1, normal.count { it.type == Material.BREAD })
    assertEquals(1, normal.count { it.type == Material.DIAMOND })
    assertTrue(normal.filter { it.type == Material.IRON_INGOT }.all { it.amount == 2 })

    val vip = table.roll(Random(2), "vip")
    assertTrue(vip.any { it.type == Material.GOLD_INGOT })
  }

  @Test
  fun `invalid rolls and bonus chances are rejected`() {
    assertFailsWith<IllegalArgumentException> { lootTable<Unit> { rolls = 0..1 } }
    assertFailsWith<IllegalArgumentException> { lootTable<Unit> { bonus(1.1) { item(Material.STONE) } } }
  }
}
