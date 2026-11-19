# merchant

[English](README.md) | [日本語]

Paper Merchantを使った取引画面をDSLで定義します。

```kotlin
player.openShop(plugin, Component.text("鉱石商")) {
  trade(ItemStack(Material.DIAMOND)) {
    cost(ItemStack(Material.EMERALD, 8))
    maxUses = 10
    experienceReward = false
  }
}
```

各 `trade` は結果アイテムを受け取り、1個目・2個目のコスト、最大使用回数、経験値報酬などを設定します。定義に渡したItemStackは内部で複製されるため、呼び出し後の変更で既存取引は変化しません。
