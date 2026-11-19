# DevCore Loot

Deterministic weighted rewards for Paper plugins. Every roll accepts a `Random`, so production can use `Random.Default` while tests and replays use a fixed seed.

```kotlin
data class DropContext(val vip: Boolean, val world: World)

val rewards = lootTable<DropContext> {
  rolls = 2..4
  weight(70) { item(Material.IRON_INGOT, 1..3) }
  weight(25) { item(Material.GOLD_INGOT, 1..2) }
  weight(5, condition = { it.vip }) { item(customRuby.create()) }
  guaranteed { item(Material.BREAD, 1..2) }
  bonus(chance = 0.10) { item(Material.DIAMOND) }
}

val result = rewards.rollTo(player.inventory, Random.Default, DropContext(player.isOp, player.world))
result.leftovers.forEach { player.world.dropItemNaturally(player.location, it) }
```

For a reusable non-item selection:

```kotlin
val weather = weighted("sun" to 8, "rain" to 2).pick(Random(seed))
```

Weights must be positive. Rolls must be a positive, non-empty range. Conditional weighted entries are removed before selection; if none are eligible, guaranteed and bonus entries still roll.
