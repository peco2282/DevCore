# update-checker

[English](README.md) | [日本語]

JDK `HttpClient`だけを使う、明示的オプトイン方式の非同期更新確認です。

```kotlin
plugin.checkForUpdates(
  Github("peco2282/MyPlugin"),
  enabled = config.getBoolean("update-checker.enabled", false)
) { result ->
  if (result.isNewer) logger.info("最新版 ${result.latest}: ${result.url}")
}
```

`enabled = true` でなければ通信しません。呼び出しごとに1回だけ非同期実行し、起動スレッドを塞ぎません。通信・HTTP・解析エラーは起動失敗にせず `null` で完了し、`FINE` に記録します。独自サービスは `UpdateSource` を実装できます。
