# DevCore

[English](README.md) | [日本語]

DevCoreプロジェクトのためのコアライブラリです。

> [!IMPORTANT]
> このプロジェクトの主要なドキュメントは**英語**で提供されています。
> 最新かつ詳細な情報については [README.md](README.md) を参照してください。

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

- Gradle プロパティ: `devcore.publish.releaseUrl`, `devcore.publish.snapshotUrl`, `devcore.publish.user`,
  `devcore.publish.password`
- 環境変数: `DEVCORE_PUBLISH_RELEASE_URL`, `DEVCORE_PUBLISH_SNAPSHOT_URL`, `DEVCORE_PUBLISH_USER`,
  `DEVCORE_PUBLISH_PASSWORD`

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
  implementation(platform("com.peco2282.devcore:devcore-bom:1.0.0"))
  implementation("com.peco2282.devcore:command")
  implementation("com.peco2282.devcore:config")
}
```

## モジュール一覧

各モジュールは特定の機能に特化しており、個別または `core` を通じて一括で導入可能です。詳細については、各モジュールのREADMEを参照してください。

### adventure

Adventureライブラリ（KyoriPowered）をKotlinから使いやすくするためのDSL。直感的なテキスト構築とスタイリングが可能です。
[詳細ドキュメント](adventure/README.ja.md)

```kotlin
val msg = component {
  text("Hello ")
  text("World") { blue(); bold() }
}
```

### command

Paper (Brigadier) コマンドを型安全なDSLで定義。引数定義や権限設定、サジェスチョンを簡潔に記述できます。
[詳細ドキュメント](command/README.ja.md)

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

YAML設定をKotlinデータクラスへ自動マッピング。アノテーションによるバリデーションとコメントの自動挿入をサポートします。
[詳細ドキュメント](config/README.ja.md)

```kotlin
@Comment("メイン設定")
data class MyConfig(@Size(min = 1) @NotEmpty val levels: List<Int> = listOf(1, 2, 3))

val config = Configs.load<MyConfig>(plugin)
```

### scheduler

Bukkit schedulerの薄いラッパー。Tickベースの時間指定や、プレイヤー/ワールドのライフサイクルに紐付いたタスク管理を提供します。
[詳細ドキュメント](scheduler/README.ja.md)

```kotlin
plugin.taskCreate after 5.seconds run {
  println("5秒後に実行")
}
player.taskTimer(plugin, 0.ticks, 20.ticks) {
  // ログアウト時に自動キャンセルされる
}
```

### cooldown

プレイヤーやシステム全般のクールダウンおよびデバウンス（連打防止）を管理するための汎用ユーティリティ。
[詳細ドキュメント](cooldown/README.ja.md)

```kotlin
val cooldowns = PlayerCooldowns()
if (cooldowns.tryUse(player, 3.seconds)) {
  player.sendMessage("スキル使用！")
}
```

### scoreboard

自動更新機能とプレイヤーごとのコンテンツ表示を備えた、サイドバーとボスバー作成のためのシンプルなDSL。
[詳細ドキュメント](scoreboard/README.ja.md)

```kotlin
val sidebar = sidebar(plugin, 20.ticks, component { text("ステータス") }) {
  line { player -> component { text("体力: ${player.health.toInt()}") } }
  emptyLine()
  line(component { text("サーバー: devcore.com") })
}
sidebar.show(player)
```

### gui

DSLと状態管理を備えたリアクティブなGUIフレームワーク。動的なタイトル更新やページネーションをサポート。
[詳細ドキュメント](gui/README.ja.md)

```kotlin
val gui = inventory(rows = 3, title = component { text("カウンター") }) {
  var count by state(0)
  slot(2, 5) {
    icon(Material.APPLE)
    name(component { text("カウント: $count") })
    onClick { count++ }
  }
}
```

### packet

PacketEventsを利用したフェイクエンティティやパケット操作のためのDSL。
[詳細ドキュメント](packet/README.ja.md)

```kotlin
player.sendFakeVisuals {
  spawnEntity(EntityType.ZOMBIE, location) {
    customName = "フェイクボス"
    isGlowing = true
  }
}
```

### entity

エンティティのスポーン、AI制御、ライフサイクル管理を簡単に行うためのモジュール。
[詳細ドキュメント](entity/README.ja.md)

```kotlin
location.spawn<Zombie> {
  isNoAi = true
  onDeath(plugin) {
    player.sendMessage("死亡しました！")
  }
}
```

### event

Bukkitイベントを簡潔かつ型安全に定義するためのDSL.
[詳細ドキュメント](event/README.ja.md)

```kotlin
on<PlayerJoinEvent> {
  handle { player.sendMessage("ようこそ！") }
}
```

### effect

パーティクルエフェクトや視覚的な強化のためのユーティリティ。
[詳細ドキュメント](effect/README.ja.md)

```kotlin
Effects.spawnCloud(location)
```

### util

コルーチンサポート、バージョンチェック、および一般的なヘルパーを提供する汎用ユーティリティ。
[詳細ドキュメント](util/README.ja.md)

```kotlin
plugin.launch {
  val data = fetchData()
}
```

### core

全モジュールを一括利用するためのアンブレラアーティファクト。
[詳細ドキュメント](core/README.ja.md)

### bom

全モジュールのバージョンを統一するためのBOM。
[詳細ドキュメント](bom/README.ja.md)

## ライセンス

Apache License 2.0。詳細は [`LICENSE`](LICENSE) をご覧ください。
