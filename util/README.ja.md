# util

バージョンチェック、安全なキャスト、および一般的なヘルパーを提供する汎用ユーティリティモジュールです。

## 使用方法

### コルーチン
> **非推奨です。** `util` の `Plugin.launch`・`Plugin.scope`・`Plugin.coroutine` は非推奨になりました。
> 代わりに `scheduler` モジュール(`com.peco2282.devcore.scheduler.coroutines`)を使ってください。
> `scheduler` 版のスコープはプラグイン無効化時に自動でキャンセルされますが、`util` 版はされません。

```kotlin
plugin.scheduler.launch {
    val data = fetchData()      // suspend 関数
    player.sendMessage("データを取得しました！")
}
```

### バージョンチェック
現在のサーバーバージョンが特定の要件を満たしているか簡単に確認できます。

```kotlin
checkVersion("1.21.1") // 未満の場合は UnsupportedVersionException をスロー
```

### キャストヘルパー
エラーハンドリングを伴う安全で便利なキャストを提供します。

```kotlin
val player = sender.asNullable<Player>()
val entity = something.asNotNullable<LivingEntity>()
```

## 特徴
- **バージョン管理**: マルチバージョン互換性を処理するための堅牢なバージョン比較。
- **内部 API ツール**: 内部限定機能のための専用アノテーション。
- **例外処理ユーティリティ**: より安全な実行のための関数型インターフェース。
