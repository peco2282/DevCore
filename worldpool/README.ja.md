# worldpool

[English](README.md) | [日本語]

ワールドテンプレートをコピーして、試合などに使う破棄可能なPaperワールドを生成します。

```kotlin
val pool = plugin.worldPool(File("templates/arena"))
val instance = pool.acquire().join()
players.forEach { it.teleport(instance.world.spawnLocation) }
instance.release(lobbySpawn).join()
pool.close()
```

テンプレート自体はライブワールドとして開きません。コピー時にはロック、UID、プレイヤーデータを除外します。コピー・削除は非同期、Bukkitのロード・アンロードとプレイヤー移動はメインスレッドで行います。本番コードでは `join()` よりFutureの連結を推奨します。インスタンスが残った状態で閉じる場合は `pool.close(lobbySpawn)` を使います。
