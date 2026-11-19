# nametag

[English](README.md) | [日本語]

スコアボードTeamを使ったプレイヤー名札と、Adventureのタブリストヘッダー・フッターを管理します。

```kotlin
plugin.nametag(player) {
  prefix = Component.text("[Admin] ", NamedTextColor.RED)
  suffix = Component.text(" ★", NamedTextColor.GOLD)
  sortKey = "00-admin"
}

val tab = plugin.tablist {
  header { Component.text("My Server") }
  footer { Component.text("${Bukkit.getOnlinePlayers().size} online") }
  refreshEvery = 20.ticks
}
tab.close()
```

`sortKey` はTeam名の先頭へ反映され、タブ表示順の制御に使えます。更新ハンドルはタスクを所有するため、不要になったら `close` してください。他プラグインが同じメインスコアボードTeamを変更する場合は競合に注意してください。
