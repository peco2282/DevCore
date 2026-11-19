# anvil-input

[English](README.md) | [日本語]

金床の名前変更欄を使い、プレイヤーから短い文字列を受け取ります。

```kotlin
player.askText(
  plugin,
  title = Component.text("名前を入力"),
  initial = player.name
) { input ->
  player.displayName(Component.text(input))
}
```

空白以外の文字列で結果スロットを押すと、コールバックが同期的に実行されます。GUIを閉じた場合はキャンセルとなり、コールバックは呼ばれません。同じプレイヤーへ再度表示すると以前のセッションを置き換えます。

PaperのバージョンでAPIが異なるため、実行環境に対応する `anvil-input:v1_20_6` または `anvil-input:v1_21_4` も同梱してください。
