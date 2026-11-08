# DevCore Storage

[English] | [[日本語](README.ja.md)]

Per-player data that is loaded before the player joins and saved when they leave, without blocking the main thread.
Use it for small and medium plugins that do not need a database.

## Install (Gradle Kotlin DSL)

```kotlin
dependencies { implementation("com.peco2282.devcore:storage:<version>") }
```

## Example

```kotlin
data class Stats(var kills: Int = 0, var coins: Long = 0, var tags: List<String> = emptyList())

val stats = plugin.playerData<Stats>()          // plugins/<plugin>/playerdata/<uuid>.yml

plugin.on<PlayerJoinEvent> {
  handle { stats[player].coins += 10 }          // plain object access; no I/O here
}
```

The data class is mapped to YAML with the `config` module, so a field added in a later version simply takes its
default value when an older file is read.

## What it does for you

| When | What happens |
|---|---|
| `AsyncPlayerPreLoginEvent` | Reads the file off the main thread. Data is ready by `PlayerJoinEvent`. |
| Player quits | Snapshot taken on the main thread, written asynchronously, then unloaded. |
| Every `autosave` (default 5 min) | Saves everyone online. |
| Plugin disables | Saves everyone and waits for the writes. |
| Re-login right after a quit | Waits for the pending save, so it never reads stale data. |

Writes go to a temporary file that is moved over the old one, so a crash cannot leave a half-written file.

## Offline players

```kotlin
stats.peek(uuid).thenAccept { data -> /* null if no data */ }       // read without caching
stats.edit(uuid) { it.coins += 100 }                                 // online or offline, then saved
stats.delete(uuid)                                                   // offline players only
```

For an offline player `edit` runs on the storage thread; do not use the Bukkit API inside it.

## When data cannot be read

By default the player is **refused at login** (`LoadFailure.KICK`) and the broken file is renamed to
`<uuid>.yml.corrupt-<time>`. Playing on defaults would overwrite the real data on the next save.
`LoadFailure.USE_DEFAULT` continues with defaults and keeps the renamed file for you to recover.

## Other storage

Implement `DataBackend<V>` (five small methods that move text) to store in a database or Redis, then
`plugin.playerData(backend, default = { Stats() })`.

## Rules

- Change the data only from the main thread. The snapshot for saving is taken there.
- `stats[player]` throws before the data is loaded (for example in a pre-login handler of your own).
