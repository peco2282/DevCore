# ranking

[English](README.md) | [日本語]

一定間隔でスコア元を読み直し、完全なランキングのスナップショットを公開します。

```kotlin
val handle = plugin.ranking<UUID>(30.seconds) { scores.toMap() }
val topTen = handle.ranking.top(10)
val ownRank = handle.ranking.find(player.uniqueId)
handle.cancel()
```

スコアは降順です。同点では入力Mapの順序を維持し、更新途中の状態は公開しません。取得関数はBukkitスケジューラ上で動くため、重いDBアクセスを直接行わないでください。
