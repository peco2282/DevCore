# loot

[English](README.md) | [日本語]

重み付き抽選、複数回抽選、条件、確定報酬、独立ボーナスを組み合わせるDSLです。

```kotlin
val rewards = lootTable<RewardContext> {
  rolls = 2..4
  weight(70) { item(Material.IRON_INGOT, 1..3) }
  weight(25) { item(Material.GOLD_INGOT, 1..2) }
  weight(5, condition = { it.vip }) { item(customRuby) }
  guaranteed { item(Material.BREAD) }
  bonus(0.10) { item(Material.DIAMOND) }
}

val result = rewards.rollTo(player.inventory, Random(seed), context)
result.leftovers.forEach { player.world.dropItemNaturally(player.location, it) }
```

`weight` 同士は重み比で1つを選び、`guaranteed` は必ず追加、`bonus` はそれぞれ独立判定です。`Random(seed)` を渡すとテストを再現できます。配布結果ではインベントリへ入った項目と余りを区別してください。
