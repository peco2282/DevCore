# kit

[English](README.md) | [日本語]

名前、権限、クールダウンを持つアイテムキットを定義し、安全にインベントリへ配布します。

```kotlin
plugin.kits().define("starter") {
  permission = "server.kit.starter"
  cooldown = 1.days
  items {
    item(Material.STONE_SWORD)
    item(Material.COOKED_BEEF, 16)
    item(customStack)
  }
}

val result = plugin.kits().give("starter", player)
if (!result.success) player.sendMessage("失敗: ${result.failure}")
```

`give` はキットの存在、権限、クールダウンの順に検査します。入り切らないアイテムは自然ドロップされ、`KitResult.dropped` から確認できます。クールダウンはメモリ上だけなので、再起動をまたぐ報酬には `persistent-cooldown` を使ってください。

## 依存関係

```kotlin
implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
implementation("com.peco2282.devcore:kit")
```
