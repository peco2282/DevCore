# ranking

[English] | [日本語](README.ja.md)

Periodically refreshed, immutable ranking snapshots.

```kotlin
val handle = plugin.ranking<UUID>(30.seconds) { scores.toMap() }
val topTen = handle.ranking.top(10)
val me = handle.ranking.find(player.uniqueId)
handle.cancel()
```

Scores are sorted descending; equal scores use stable source iteration order. Readers always see a complete snapshot. The source runs on the Bukkit scheduler, so fetch database data asynchronously before returning it.
