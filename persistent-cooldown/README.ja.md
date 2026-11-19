# persistent-cooldown

[English](README.md) | [日本語]

プレイヤーPDCに期限時刻を保存し、再起動後も残るクールダウンを提供します。

```kotlin
val daily = PersistentCooldowns(plugin)
if (daily.tryUse(player, "daily-reward", 1.days)) giveReward(player)
else player.sendMessage("残り: ${daily.remaining(player, "daily-reward")}")
daily.clear(player, "daily-reward")
```

キーはプラグイン名前空間に保存され、期限切れ値は読み取り時に除去されます。OS時刻の変更の影響を受けるため、日次報酬向けであり短時間の戦闘判定には通常の `cooldown` を使ってください。
