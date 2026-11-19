# teleport

[English] | [日本語](README.ja.md)

Safe-destination searches and cancellable teleport warmups.

```kotlin
player.teleportWithWarmup(plugin, destination, 5.seconds) {
  cancelOnMove = true
  cancelOnDamage = true
  requireSafeDestination = true
  onTick { remaining -> player.sendActionBar(Component.text("$remaining")) }
  onCancel { reason -> player.sendMessage("Cancelled: $reason") }
  onComplete { player.sendMessage("Teleported") }
}

val safe = destination.findSafe(radius = 5, verticalRadius = 8)
player.teleportSafely(destination)
```

Warmup handles can be cancelled explicitly. Safety requires solid ground, two passable body blocks and a loaded valid world; `findSafe` returns `null` when no candidate is found.
