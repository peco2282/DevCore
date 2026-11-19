# kit

[English] | [日本語](README.ja.md)

Named, permission-aware item kits with cooldowns and safe inventory delivery.

```kotlin
val kits = plugin.kits()
kits.define("starter") {
  permission = "server.kit.starter"
  cooldown = 1.days
  items {
    item(Material.STONE_SWORD)
    item(Material.COOKED_BEEF, 16)
    item(customStack)
  }
}

val result = kits.give("starter", player)
if (!result.success) player.sendMessage("Kit failed: ${result.failure}")
result.dropped.forEach { player.sendMessage("Inventory full: ${it.type}") }
```

`define` replaces a definition with the same name. `give` checks existence, permission, then cooldown. Items that do not fit are dropped naturally and reported in `KitResult.dropped`. Cooldowns are in memory; use `persistent-cooldown` for restart-resistant rewards.

## Dependency

```kotlin
implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
implementation("com.peco2282.devcore:kit")
```
