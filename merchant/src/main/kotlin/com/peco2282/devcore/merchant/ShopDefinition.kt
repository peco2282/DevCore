package com.peco2282.devcore.merchant

import io.papermc.paper.event.player.PlayerPurchaseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe

/** One merchant recipe and its optional completion callback. */
data class TradeDefinition(
  val recipe: MerchantRecipe,
  val onTrade: ((PlayerPurchaseEvent) -> Unit)?
)

/** Immutable recipes used to open a merchant. */
data class ShopDefinition(val trades: List<TradeDefinition>)

/** Builds a reusable shop definition without opening an inventory. */
fun shopDefinition(block: ShopBuilder.() -> Unit): ShopDefinition = ShopBuilder().apply(block).build()

/** Merchant recipe DSL. */
class ShopBuilder {
  private val trades = mutableListOf<TradeDefinition>()

  fun trade(give: ItemStack, receive: ItemStack, block: TradeBuilder.() -> Unit = {}) =
    trade(listOf(give), receive, block)

  fun trade(give: List<ItemStack>, receive: ItemStack, block: TradeBuilder.() -> Unit = {}) {
    require(give.size in 1..2) { "A merchant trade requires one or two ingredients" }
    require(give.none { it.type.isAir || it.amount <= 0 }) { "Trade ingredients must be non-air stacks" }
    require(!receive.type.isAir && receive.amount > 0) { "Trade result must be a non-air stack" }
    trades += TradeBuilder(give, receive).apply(block).build()
  }

  internal fun build(): ShopDefinition {
    require(trades.isNotEmpty()) { "A shop requires at least one trade" }
    return ShopDefinition(trades.toList())
  }
}

/** Options for one merchant trade. */
class TradeBuilder internal constructor(
  private val ingredients: List<ItemStack>,
  private val result: ItemStack
) {
  var uses: Int = 0
  var maxUses: Int = Int.MAX_VALUE
  var experienceReward: Boolean = true
  var villagerExperience: Int = 0
  var priceMultiplier: Float = 0f
  var demand: Int = 0
  var specialPrice: Int = 0
  var ignoreDiscounts: Boolean = false
  private var callback: ((PlayerPurchaseEvent) -> Unit)? = null

  fun onTrade(block: (PlayerPurchaseEvent) -> Unit) { callback = block }

  internal fun build(): TradeDefinition {
    require(uses >= 0 && maxUses > 0 && uses <= maxUses) { "uses must be within 0..maxUses" }
    require(villagerExperience >= 0) { "villagerExperience must be non-negative" }
    require(priceMultiplier >= 0f) { "priceMultiplier must be non-negative" }
    val recipe = MerchantRecipe(result.clone(), uses, maxUses, experienceReward, villagerExperience, priceMultiplier)
    recipe.ingredients = ingredients.map(ItemStack::clone)
    recipe.demand = demand
    recipe.specialPrice = specialPrice
    recipe.setIgnoreDiscounts(ignoreDiscounts)
    return TradeDefinition(recipe, callback)
  }
}
