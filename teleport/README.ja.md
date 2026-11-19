# teleport

[English](README.md) | [日本語]

安全地点の検索と、移動・ダメージで中止できるテレポート待機処理です。

```kotlin
player.teleportWithWarmup(plugin, destination, 5.seconds) {
  cancelOnMove = true
  cancelOnDamage = true
  requireSafeDestination = true
  onCancel { reason -> player.sendMessage("中止: $reason") }
  onComplete { player.sendMessage("移動しました") }
}
```

安全地点は足元が固体で、身体2ブロックが通過可能であることを確認します。候補がなければ `findSafe` は `null` を返します。戻り値のハンドルから明示的に中止することもできます。
