# debug

[English](README.md) | [日本語]

カテゴリ単位で実行時に有効化できるデバッグログと計測補助です。

```kotlin
plugin.debugController().enable("storage")
val log = plugin.debugLogger("storage")
log.debug { "${player.uniqueId} を読み込みます" }
val value = log.timed("player load") { repository.load(player.uniqueId) }
```

無効なカテゴリではメッセージのラムダ自体を評価しません。`timed` は経過時間を出力して結果を返し、例外は握り潰さず呼び出し側へ返します。
