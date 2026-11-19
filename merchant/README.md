# DevCore Merchant

Build and open custom merchant inventories without manually assembling `MerchantRecipe` objects.

```kotlin
player.openShop(plugin, text("Reward Exchange")) {
  trade(ItemStack(Material.EMERALD, 5), ItemStack(Material.DIAMOND)) {
    maxUses = 10
    experienceReward = false
    onTrade { event -> audit(event.player, event.trade) }
  }

  trade(
    give = listOf(ItemStack(Material.IRON_INGOT, 8), ItemStack(Material.COAL, 4)),
    receive = ItemStack(Material.DIAMOND_PICKAXE)
  ) {
    demand = 1
    priceMultiplier = 0.05f
    ignoreDiscounts = true
  }
}
```

Callbacks are routed from Paper's `PlayerPurchaseEvent` only for recipes created by this module. Listener and callback state are scoped to the owning plugin and cleared when it disables. A trade accepts exactly one or two non-air ingredients.
