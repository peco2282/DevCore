# cache

[English](README.md) | [日本語]

計算結果や外部データを短時間保持する、スレッドセーフなTTLキャッシュです。

```kotlin
val names = ttlCache<UUID, String>(5.minutes, maxSize = 500)
names.put(player.uniqueId, player.name)
val name = names.getOrPut(player.uniqueId) { loadName(player.uniqueId) }
names.invalidate(player.uniqueId)
```

期限切れはアクセス時に除去され、上限超過時は古いエントリから削除されます。同時ミス時には `getOrPut` の生成処理が複数回走る可能性があるため、再実行可能な処理を渡してください。永続ストレージの代わりにはなりません。
