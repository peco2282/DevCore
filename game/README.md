# game

[English] | [日本語](README.ja.md)

A small minigame state machine with participant tracking and lifecycle-owned tasks/listeners.

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
game.finish()
game.leave(player)
game.close()
```

The legal flow is `LOBBY → COUNTDOWN → RUNNING → ENDING → LOBBY`. Falling below the minimum during countdown returns to the lobby. `close` cancels owned work and should be called when the arena/plugin feature is removed.
