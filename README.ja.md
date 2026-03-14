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

各モジュールは個別、または `core` を通じて一括で導入可能です。

- [**adventure**](#adventure): AdventureライブラリをKotlinから使いやすくするためのDSL。直感的なテキスト構築とスタイリングが可能です。
- [**command**](#command): Paper (Brigadier) コマンドを型安全なDSLで定義。引数定義や権限設定、サジェスチョンを簡潔に記述できます。
- [**config**](#config): YAML設定をKotlinデータクラスへ自動マッピング。アノテーションによるバリデーションとコメントの自動挿入をサポートします。
- [**scheduler**](#scheduler): Bukkit schedulerの薄いラッパー。Tickベースの時間指定や、プレイヤー/ワールドのライフサイクルに紐付いたタスク管理を提供します。
- [**cooldown**](#cooldown): プレイヤーやシステム全般のクールダウンおよびデバウンス（連打防止）を管理するための汎用ユーティリティ。
- [**gui**](#gui): Reactive GUI framework with DSL and State management. 動的なタイトル更新やページネーションをサポート。
- [**packet**](#packet): PacketEventsを利用したフェイクエンティティやパケット操作のためのDSL。
- [**event**](#event): Bukkitイベントを簡潔かつ型安全に定義するためのDSL。
- [**effect**](#effect): パーティクルエフェクトや視覚的な強化のためのユーティリティ。
- [**core**](#core): 全モジュールを一括利用するためのアンブレラアーティファクト。
- [**bom**](#bom): 全モジュールのバージョンを統一するためのBOM。

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

### scoreboard

[モジュールへ移動](scoreboard/README.ja.md)

```kotlin
val sidebar = sidebar(plugin, 20.ticks, component { text("ステータス") }) {
  line { player -> component { text("体力: ${player.health.toInt()}") } }
  emptyLine()
  line(component { text("サーバー: devcore.com") })
}
sidebar.show(player)

val bar = bossBar(plugin) {
  title { player -> component { text("HP: ${player.health.toInt()}") } }
  progress { player -> (player.health / 20.0).toFloat() }
  red()
  autoRefresh(plugin, 20.ticks)
}
bar.show(player)
```

### gui

[モジュールへ移動](gui/README.ja.md)

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

[モジュールへ移動](packet/README.ja.md)

```kotlin
player.sendFakeVisuals {
  spawnEntity(EntityType.ZOMBIE, location) {
    customName = "フェイクボス"
    isGlowing = true
  }
}
```

### event

[モジュールへ移動](event/README.ja.md)

```kotlin
on<PlayerJoinEvent> {
  handle { player.sendMessage("ようこそ！") }
}
```

### effect

[モジュールへ移動](effect/README.ja.md)

パーティクルエフェクトや視覚的な強化のためのユーティリティを提供します。

### core

[モジュールへ移動](core/README.ja.md)
全モジュールを一括で利用するためのアーティファクトです。

### bom

[モジュールへ移動](bom/README.ja.md)
バージョンを統一するためのBOM。

## ライセンス

Apache License 2.0。詳細は [`LICENSE`](LICENSE) をご覧ください。
