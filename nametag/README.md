# DevCore Nametag

Manage scoreboard-team name decoration without replacing a player's scoreboard, plus periodically refreshed viewer-specific tab headers and footers.

```kotlin
// Usually in PlayerJoinEvent or after a rank change.
plugin.nametag(player) {
  prefix = text("[VIP] ", NamedTextColor.GOLD)
  suffix = text(" ♥", NamedTextColor.RED)
  color = NamedTextColor.YELLOW
  sortKey = "01-vip"
}

val tab = plugin.tablist {
  refreshTicks = 20
  header(text("My Server", NamedTextColor.GOLD))
  footer { viewer -> text("Ping: ${viewer.ping}ms") }
}

// Optional early cleanup; plugin disable also stops updates.
tab.remove()
plugin.nametags().remove(player)
```

The manager reconciles tags against every online viewer's current scoreboard. This allows coexistence with `scoreboard-lite` and other code that assigns per-player scoreboards. `sortKey` is limited to 1–8 safe ASCII characters so the generated team name remains within Minecraft's 16-character limit.
