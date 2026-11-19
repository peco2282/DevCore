# persistent-cooldown

[English] | [日本語](README.ja.md)

Restart-resistant player cooldowns stored as epoch deadlines in Persistent Data Container.

```kotlin
val daily = PersistentCooldowns(plugin)
if (daily.tryUse(player, "daily-reward", 1.days)) giveReward(player)
else player.sendMessage("Wait ${daily.remaining(player, "daily-reward")}")
daily.clear(player, "daily-reward")
```

Keys are namespaced to the plugin. Expired values are removed when read. Wall-clock changes affect epoch-based deadlines; this module is suitable for daily rewards, not precise combat timing.
