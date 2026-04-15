# util

コルーチンサポート、バージョンチェック、および一般的なヘルパーを提供する汎用ユーティリティモジュールです。

## 使用方法

### コルーチン
Bukkit スレッドに対応したコルーチンへのアクセスを簡素化します。

```kotlin
plugin.launch {
    // デフォルトで Bukkit メインスレッドで実行されます
    val data = fetchDataAsync() // suspend 関数
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
- **Bukkit コルーチンディスパッチャ**: Minecraft メインスレッド上での Kotlin コルーチンとのシームレスな統合。
- **バージョン管理**: マルチバージョン互換性を処理するための堅牢なバージョン比較。
- **内部 API ツール**: 内部限定機能のための専用アノテーション。
- **例外処理ユーティリティ**: より安全な実行のための関数型インターフェース。
