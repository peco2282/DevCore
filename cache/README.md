# cache

[English] | [日本語](README.ja.md)

Small thread-safe TTL caches for derived or remote data.

```kotlin
val names = ttlCache<UUID, String>(ttl = 5.minutes, maxSize = 500)
names.put(player.uniqueId, player.name)
val cached = names.get(player.uniqueId)
val value = names.getOrPut(id) { loadName(id) }
names.invalidate(id)
names.clear()
```

Entries expire lazily when accessed and the oldest entries are removed when `maxSize` is exceeded. `getOrPut` may execute its producer more than once under concurrent misses, so producers should be safe to repeat. Do not use this cache as durable storage.

## Dependency

```kotlin
implementation("com.peco2282.devcore:cache:<version>")
```
