# display

[English](README.md) | [日本語]

PaperのText・Item・Block Displayと、管理されたホログラムを構築するDSLです。

```kotlin
val hologram = plugin.hologram(location) {
  line(Component.text("Arena", NamedTextColor.GOLD))
  dynamic { Component.text("参加者: ${arena.players.size}") }
  spacing = 0.28
  refreshEvery = 10.ticks
}
hologram.teleport(newLocation)
hologram.remove()
```

動的行は指定間隔で再評価されます。ハンドルがエンティティと更新タスクを所有するため、機能終了時は `remove` を呼んでください。変形はJOMLのベクトル・クォータニオンで指定します。
