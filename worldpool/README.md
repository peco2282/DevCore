# worldpool

[English] | [日本語](README.ja.md)

Creates disposable Paper worlds by copying a world template.

```kotlin
val pool = plugin.worldPool(File("templates/arena"))
val instance = pool.acquire().join()
players.forEach { it.teleport(instance.world.spawnLocation) }
instance.release(lobbySpawn).join() // moves players, unloads, then deletes the copy
pool.close() // valid after every instance has been released
```

The template is never opened as the live world. Session-lock, UID and player-data files are excluded while copying. Copy/delete work is asynchronous; Bukkit world load/unload and player teleporting are returned to the main thread. Prefer future composition to `join()` in production code. To close with active instances, use `pool.close(lobbySpawn)`.
