# game

[English](README.md) | [日本語]

参加者と所有リソースを管理する、小さなミニゲーム状態機械です。

```kotlin
val game = plugin.minigame("spleef") {
  lobby { minimumPlayers = 2 }
  countdown(10.seconds)
  running { timeout = 5.minutes }
  ending { duration = 5.seconds }
  teams { assign = Assignment.BALANCED }
}
game.join(player)
game.startCountdown()
```

状態は `LOBBY → COUNTDOWN → RUNNING → ENDING → LOBBY` です。カウントダウン中に最低人数を下回るとロビーへ戻ります。アリーナを破棄するときは `close` で所有タスク等を停止してください。
