package com.peco2282.devcore.merchant

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MerchantRecipeTest {
  @Test
  fun `trade maps ingredients and recipe options`() {
    val shop = shopDefinition {
      trade(ItemStack(Material.EMERALD, 5), ItemStack(Material.DIAMOND)) {
        maxUses = 10
        experienceReward = false
        villagerExperience = 3
        priceMultiplier = 0.25f
        ignoreDiscounts = true
      }
      trade(
        listOf(ItemStack(Material.IRON_INGOT, 8), ItemStack(Material.COAL, 4)),
        ItemStack(Material.DIAMOND_PICKAXE)
      )
    }

    val first = shop.trades.first().recipe
    assertEquals(2, shop.trades.size)
    assertEquals(5, first.ingredients.single().amount)
    assertEquals(10, first.maxUses)
    assertFalse(first.hasExperienceReward())
    assertEquals(3, first.villagerExperience)
    assertEquals(0.25f, first.priceMultiplier)
    assertTrue(first.shouldIgnoreDiscounts())
  }

  @Test
  fun `merchant accepts only one or two non-air ingredients`() {
    assertFailsWith<IllegalArgumentException> {
      shopDefinition { trade(emptyList(), ItemStack(Material.DIAMOND)) }
    }
    assertFailsWith<IllegalArgumentException> {
      shopDefinition { trade(List(3) { ItemStack(Material.EMERALD) }, ItemStack(Material.DIAMOND)) }
    }
  }
}
