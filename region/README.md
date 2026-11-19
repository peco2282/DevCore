# region

[English] | [日本語](README.ja.md)

Pure geometric regions plus enter, leave and movement event tracking.

```kotlin
val spawn = cuboid(world, first, second)
plugin.regions().register("spawn", spawn) {
  onEnter { player.sendMessage("Welcome") }
  onLeave { player.sendMessage("Goodbye") }
}

if (spawn.contains(player.location)) protect(player)
plugin.regions().unregister("spawn")
```

`cuboid`, `sphere` and `cylinder` normalize their bounds. A region belongs to one world. The registry compares membership during movement and teleport events; unregister regions that are no longer needed.
