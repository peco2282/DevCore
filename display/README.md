# display

[English] | [日本語](README.ja.md)

DSLs for Paper text, item and block display entities, including managed holograms.

```kotlin
val hologram = plugin.hologram(location) {
  line(Component.text("Arena", NamedTextColor.GOLD))
  dynamic { Component.text("Players: ${arena.players.size}") }
  spacing = 0.28
  refreshEvery = 10.ticks
}
hologram.teleport(newLocation)
hologram.remove()

plugin.itemDisplay(location) {
  item = ItemStack(Material.DIAMOND)
  scale = Vector3f(1.5f, 1.5f, 1.5f)
  spinEvery = 2.ticks
}
```

Dynamic hologram lines are reevaluated on the configured interval. Handles own their entities and tasks; call `remove` when the feature ends. Display transforms use JOML vectors/quaternions and are applied when spawned.
