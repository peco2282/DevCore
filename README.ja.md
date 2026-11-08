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

## Before / After

DevCore は、Paper/Bukkit で繰り返し書くコードを短い DSL に置き換えます。同じ動作を両方で書くとこうなります(以下4例の合計で 52 行 → 18 行):

### イベント(3回だけ、OPのみ処理)

**Before** — Paper / Bukkit API のみ(11 行)

```kotlin
class JoinListener : Listener {
  private var count = 0

  @EventHandler
  fun onJoin(event: PlayerJoinEvent) {
    if (!event.player.isOp) return
    event.player.sendMessage("Welcome, operator!")
    if (++count >= 3) HandlerList.unregisterAll(this)
  }
}

// onEnable
server.pluginManager.registerEvents(JoinListener(), this)
```

**After** — DevCore(5 行)

```kotlin
plugin.on<PlayerJoinEvent> {
  filter { player.isOp }
  take(3)
  handle { player.sendMessage("Welcome, operator!") }
}
```

### プレイヤーに紐づく繰り返しタスク(ログアウトで停止)

**Before** — Paper / Bukkit API のみ(11 行)

```kotlin
val task = object : BukkitRunnable() {
  override fun run() {
    player.sendActionBar(Component.text("tick"))
  }
}.runTaskTimer(plugin, 0L, 20L)

server.pluginManager.registerEvents(object : Listener {
  @EventHandler
  fun onQuit(e: PlayerQuitEvent) {
    if (e.player == player) task.cancel()
  }
}, plugin)
```

**After** — DevCore(3 行)

```kotlin
player.taskTimer(plugin, 0.ticks, 20.ticks) {
  player.sendActionBar(Component.text("tick"))
}
```

### プレイヤーを5秒間追従するリング状パーティクル

**Before** — Paper / Bukkit API のみ(16 行)

```kotlin
object : BukkitRunnable() {
  var elapsed = 0

  override fun run() {
    if (!player.isValid || elapsed >= 100) {
      cancel()
      return
    }
    val center = player.location.add(0.0, 1.0, 0.0)
    for (i in 0 until 36) {
      val angle = 2 * Math.PI * i / 36
      val at = center.clone().add(cos(angle) * 1.5, 0.0, sin(angle) * 1.5)
      player.world.spawnParticle(Particle.END_ROD, at, 1)
    }
    elapsed += 2
  }
}.runTaskTimer(plugin, 0L, 2L)
```

**After** — DevCore(2 行)

```kotlin
val ring = effect { particle(Particle.END_ROD, circle(1.5).offset(y = 1.0)) }
plugin.playEffect(ring, player, every = 2.ticks, duration = 5.seconds)
```

### コマンド(プレイヤー引数つき)

**Before** — Paper / Bukkit API のみ(14 行)

```kotlin
lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
  event.registrar().register(
    Commands.literal("greet")
      .then(
        Commands.argument("target", ArgumentTypes.player())
          .executes { ctx ->
            val target = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java)
              .resolve(ctx.source).first()
            ctx.source.sender.sendMessage("Hello, ${target.name}")
            Command.SINGLE_SUCCESS
          }
      ).build()
  )
}
```

**After** — DevCore(8 行)

```kotlin
plugin.command("greet") {
  player("target") {
    executes { ctx ->
      ctx.sendSuccess { text("Hello, ${ctx.getPlayer("target")?.name}") }
      1
    }
  }
}
```

## どのモジュールが必要?

| モジュール | 省けるボイラープレート | `core` に含まれる |
|---|---|:---:|
| `adventure` | Adventure コンポーネントの組み立てとスタイル指定 | ✅ |
| `command` | Brigadier のコマンドツリー・引数・suspend ハンドラ | ✅ |
| `config` | YAML ↔ data class のマッピング・検証・コメント | ✅ |
| `scheduler` | `BukkitRunnable`、ライフサイクル連動タスク、コルーチン Dispatcher | ✅ |
| `task-sequence` | 「待って、次にこれ」を連ねるタイムライン | ✅ |
| `event` | `Listener` クラス、`@EventHandler`、手動の登録解除 | ✅ |
| `cooldown` | プレイヤーごとのクールダウン/デバウンス管理 | ✅ |
| `gui` | インベントリのクリック処理と再描画 | ✅ |
| `scoreboard` | サイドバーとボスバー(`core` には `scoreboard-lite` のみ) | ✅ (lite) |
| `effect` | パーティクル形状の計算、繰り返し・追従再生 | ✅ |
| `packet` | PacketEvents 経由の偽エンティティ/ブロック | – |
| `entity` | スポーン、AI ゴール、ライフサイクルフック | – |
| `world` | ブロック/チャンク/プレイヤーの編集 | – |
| `database` | 接続設定、マイグレーション、リポジトリ | – |
| `i18n` | 言語ファイル、フォールバック、MiniMessage プレースホルダ | – |
| `pdc` | `NamespacedKey` と `PersistentDataType` の組、メタの取得・再設定の往復 | – |
| `item` | カスタムアイテムの ID、リスナー、レシピ | – |
| `storage` | プレイヤーデータの非同期読み書き、オートセーブ、安全な書き込み | – |
| `util` | キャスト、バージョンチェック、小さなヘルパー | – |

「–」のモジュールは `core` に含まれません。個別に追加してください(バージョンは BOM で揃います)。

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
  implementation("com.peco2282.devcore:core:1.2.0")
}
```

BOMを使用してモジュールを選択する場合:

```kotlin
dependencies {
  implementation(platform("com.peco2282.devcore:devcore-bom:1.2.0"))
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

### task-sequence

時系列に沿った演出（シーケンス）を記述するためのモジュールです。コルーチンをベースとし、「10ティック後に音を鳴らす」「その5ティック後に爆発エフェクト」といった一連の流れを直感的に記述できます。
[詳細ドキュメント](task-sequence/README.ja.md)

```kotlin
plugin.sequence {
  player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
  wait(10.ticks)
  location.spawnParticle(Particle.EXPLOSION, 10, 0.5, 0.5, 0.5)
  wait(5.ticks)
  location.createExplosion(2f)
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

### world

ワールド、チャンク、ブロック、プレイヤーを型安全に編集するためのDSL。
[詳細ドキュメント](world/README.ja.md)

### database

データベース抽象化と、JDBC、HikariCP、MySQL、SQLite、Redis、MongoDB向けのDSLアダプター。
[詳細ドキュメント](database/README.ja.md)

### i18n

ロケールのフォールバックとメッセージ引数に対応した、Bukkitプラグイン向けYAML国際化機能。
[詳細ドキュメント](i18n/README.ja.md)

### event

Bukkitイベントを簡潔かつ型安全に定義するためのDSL.
[詳細ドキュメント](event/README.ja.md)

```kotlin
on<PlayerJoinEvent> {
  handle { player.sendMessage("ようこそ！") }
}
```

### effect

形状ジェネレータ(円・球・らせん・線)を備えた、再利用可能なパーティクル/サウンドエフェクトと、繰り返し・追従再生。
[詳細ドキュメント](effect/README.ja.md)

```kotlin
val ring = effect {
  particle(Particle.END_ROD, circle(1.5).offset(y = 1.0))
  sound(Sound.ENTITY_PLAYER_LEVELUP)
}
plugin.playEffect(ring, player, every = 2.ticks, duration = 5.seconds)
```

### pdc

プレイヤー・エンティティ・アイテム・ブロックの `PersistentDataContainer` を型安全に扱い、自動保存されるプロパティも提供します。
[詳細ドキュメント](pdc/README.ja.md)

```kotlin
var Player.kills: Int by pdc(plugin, "kills", default = 0)
player.kills++      // 永続化されます
```

### item

見た目・振る舞い・レシピを一箇所にまとめたカスタムアイテム。
[詳細ドキュメント](item/README.ja.md)

```kotlin
items.register("ruby_sword", Material.DIAMOND_SWORD) {
  appearance { displayName = Component.text("ルビーの剣") }
  onAttack { it.damage *= 1.5 }
}
```

### storage

参加前に読み込み、非同期で保存するプレイヤーデータ。オートセーブとクラッシュに強い書き込みつき。
[詳細ドキュメント](storage/README.ja.md)

```kotlin
val stats = plugin.playerData<Stats>()
stats[player].coins += 10
```

### util

バージョンチェック、安全なキャスト、一般的なヘルパーを提供する汎用ユーティリティ。(コルーチン対応は `scheduler` にあります。)
[詳細ドキュメント](util/README.ja.md)

```kotlin
checkVersion("1.21.1") // throws UnsupportedVersionException if older
val player = sender.asNullable<Player>()
```

### core

全モジュールを一括利用するためのアンブレラアーティファクト。
[詳細ドキュメント](core/README.ja.md)

### bom

全モジュールのバージョンを統一するためのBOM。
[詳細ドキュメント](bom/README.ja.md)

## ライセンス

Apache License 2.0。詳細は [`LICENSE`](LICENSE) をご覧ください。
