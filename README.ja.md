# DevCore
[[English](README.md)] | [日本語]

DevCoreプロジェクトのためのコアライブラリです。

## ドキュメント
- [Documentation](https://peco2282.github.io/DevCore/)

## 必要要件
- JDK 21+

## ビルド
```bash
./gradlew build
```

## 公開 (Publish)
Gradle プロパティまたは環境変数を設定し、`./gradlew publish` を実行してください。

- Gradle プロパティ: `devcore.publish.releaseUrl`, `devcore.publish.snapshotUrl`, `devcore.publish.user`, `devcore.publish.password`
- 環境変数: `DEVCORE_PUBLISH_RELEASE_URL`, `DEVCORE_PUBLISH_SNAPSHOT_URL`, `DEVCORE_PUBLISH_USER`, `DEVCORE_PUBLISH_PASSWORD`

## 使用方法 (Gradle)

リポジトリ:

```kotlin
repositories {
  maven("https://maven.peco2282.com/maven-releases/")
}
```

単一の依存関係を使用する場合:

```kotlin
dependencies {
  implementation("com.peco2282.devcore:core:1.0.0")
}
```

BOMを使用してモジュールを選択する場合:

```kotlin
dependencies {
  implementation(platform("com.peco2282.devcore:bom:1.0.0"))
  implementation("com.peco2282.devcore:command")
  implementation("com.peco2282.devcore:config")
}
```

## モジュール一覧

各モジュールは個別、または `core` を通じて一括で導入可能です。

- [**adventure**](#adventure): AdventureライブラリをKotlinから使いやすくするためのDSL。直感的なテキスト構築とスタイリングが可能です。
- [**command**](#command): Paper (Brigadier) コマンドを型安全なDSLで定義。引数定義や権限設定、サジェスチョンを簡潔に記述できます。
- [**config**](#config): YAML設定をKotlinデータクラスへ自動マッピング。アノテーションによるバリデーションとコメントの自動挿入をサポートします。
- [**scheduler**](#scheduler): Bukkit schedulerの薄いラッパー。Tickベースの時間指定や、プレイヤー/ワールドのライフサイクルに紐付いたタスク管理を提供します。
- [**cooldown**](#cooldown): プレイヤーやシステム全般のクールダウンおよびデバウンス（連打防止）を管理するための汎用ユーティリティ。
- [**core**](#core): 全モジュールを一括利用するためのアンブレラアーティファクト。

---

### adventure
[モジュールへ移動](adventure/README.ja.md)
```kotlin
val msg = component {
  text("Hello ")
  text("World") { blue(); bold() }
}
```

### command
[モジュールへ移動](command/README.ja.md)
```kotlin
plugin.command("test") {
  player("target") {
    executes { context ->
      val target = context.getPlayer("target")
      context.sendSuccess { text("Hello, ${target?.name}") }
      1
    }
  }
}
```

### config
[モジュールへ移動](config/README.ja.md)
```kotlin
@Comment("メイン設定")
data class MyConfig(@Size(min = 1) @NotEmpty val levels: List<Int> = listOf(1, 2, 3))

val config = Configs.load<MyConfig>(plugin)
```

### scheduler
[モジュールへ移動](scheduler/README.ja.md)
```kotlin
plugin.taskCreate after 5.seconds run {
  println("5秒後に実行")
}
player.taskTimer(plugin, 0.ticks, 20.ticks) {
  // ログアウト時に自動キャンセルされる
}
```

### cooldown
[モジュールへ移動](cooldown/README.ja.md)
```kotlin
val cooldowns = PlayerCooldowns()
if (cooldowns.tryUse(player, 3.seconds)) {
  player.sendMessage("スキル使用！")
}
```

### core
[モジュールへ移動](core/README.ja.md)
全モジュールを一括で利用するためのアーティファクトです。

## ライセンス
Apache License 2.0。詳細は [`LICENSE`](LICENSE) をご覧ください。
