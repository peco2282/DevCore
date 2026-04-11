# DevCore Command

[[English](README.md)] | [日本語]

Paper (Brigadier) コマンドを Kotlin DSL で定義するためのモジュールです。

## 特徴

- Brigadierをベースとした型安全なDSL
- 直感的な引数定義（string, integer, player等）
- 条件ベースの実行制限（permission, op等）
- 高度なサジェスチョン（タブ補完）機能
- 実行対象の限定（executesPlayer, executesConsole）
- `DevCoreArgumentTypes` によるバージョン透過的なNMS引数タイプ

## Install (Gradle Kotlin DSL)

```kotlin
repositories {
  maven("https://maven.peco2282.com/maven-releases/")
}

dependencies {
  implementation("com.peco2282.devcore:command:<version>")
  // または BOM を使用:
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  // implementation("com.peco2282.devcore:command")
}
```

---

## 使用方法

### コマンドの登録

`plugin.command(name) { ... }` 拡張関数を使ってコマンドを定義・登録します。
DSLブロック内では `CommandCreator` を通じてコマンドツリー全体を構成します。

```kotlin
plugin.command("hello") {
  executes { context ->
    context.source.sender.sendMessage("Hello, World!")
    1
  }
}
```

---

### リテラルサブコマンド

`literal` または `"name" { }` 演算子ショートハンドでサブコマンドを追加できます。

```kotlin
plugin.command("admin") {
  requireOp()

  literal("reload") {
    executes { context ->
      context.sendSuccess { text("設定を再読み込みしました。") }
      1
    }
  }

  "info" {
    executes { context ->
      context.sendSuccess { text("サーバー情報はこちら。") }
      1
    }
  }
}
```

---

### 引数

#### DSL組み込み引数ヘルパー

以下の引数ヘルパーがDSL内で直接使用できます。

| メソッド | 型 | 説明 |
|---|---|---|
| `string(name)` | `String` | クォートあり・なしの文字列 |
| `word(name)` | `String` | スペースなしの単語 |
| `greedyString(name)` | `String` | 残りの入力すべてを1つの文字列として取得 |
| `integer(name, min, max)` | `Int` | 範囲指定可能な整数 |
| `long(name, min, max)` | `Long` | 範囲指定可能なLong |
| `float(name, min, max)` | `Float` | 範囲指定可能なFloat |
| `double(name, min, max)` | `Double` | 範囲指定可能なDouble |
| `boolean(name)` | `Boolean` | `true` または `false` |
| `player(name)` | `PlayerSelectorArgumentResolver` | 単一プレイヤーセレクター |
| `players(name)` | `PlayerSelectorArgumentResolver` | 複数プレイヤーセレクター |
| `entity(name)` | `EntitySelectorArgumentResolver` | 単一エンティティセレクター |
| `entities(name)` | `EntitySelectorArgumentResolver` | 複数エンティティセレクター |
| `world(name)` | `World` | ロード済みワールド |
| `blockPos(name)` | `BlockPositionResolver` | 整数ブロック座標 (X Y Z) |
| `finePos(name)` | `FinePositionResolver` | 小数座標 (X Y Z) |
| `rotation(name)` | `RotationResolver` | ヨーとピッチの角度 |

```kotlin
plugin.command("givemoney") {
  permission("myplugin.admin")

  player("target") {
    integer("amount", min = 1) {
      executes { context ->
        val players = context.getArgument("target", PlayerSelectorArgumentResolver::class.java)
          .resolve(context.source)
        val amount = context.getArgument("amount", Int::class.java)
        players.forEach { it.sendMessage("${amount}円を受け取りました！") }
        1
      }
    }
  }
}
```

---

### DevCoreArgumentTypes

`DevCoreArgumentTypes` は、DSLヘルパーとして提供されていない追加のMinecraft/NMS引数タイプを提供します。
実行時に自動でバージョン固有の実装（`1.20.x` および `1.21+` 対応）を選択します。

汎用の `argument(name, type) { }` ビルダーと組み合わせて使用します。

```kotlin
import com.peco2282.devcore.command.argument.DevCoreArgumentTypes
import com.peco2282.devcore.command.argument.FinePositionResolver
import com.peco2282.devcore.command.argument.RotationResolver
import com.peco2282.devcore.command.argument.ColumnBlockPositionResolver
```

#### 小数座標 (finePosition)

プレイヤーを指定した精密座標にテレポートする例:

```kotlin
plugin.command("tp") {
  argument("pos", DevCoreArgumentTypes.finePosition(centerIntegers = true)) {
    executes { context ->
      val resolver = context.getArgument("pos", FinePositionResolver::class.java)
      val pos = resolver.resolve(context.source)
      val player = context.source.sender as? Player ?: return@executes 0
      player.teleport(pos.toLocation(player.world))
      1
    }
  }
}
```

#### 回転 (rotation)

プレイヤーの向きを変更する例:

```kotlin
plugin.command("setrot") {
  argument("rot", DevCoreArgumentTypes.rotation()) {
    executes { context ->
      val resolver = context.getArgument("rot", RotationResolver::class.java)
      val rot = resolver.resolve(context.source)
      val player = context.source.sender as? Player ?: return@executes 0
      player.teleport(player.location.apply { yaw = rot.yaw; pitch = rot.pitch })
      1
    }
  }
}
```

#### 列ブロック座標 (columnBlockPosition)

X・Z座標のみを受け取り、Y座標は手動で指定する例:

```kotlin
plugin.command("column") {
  argument("col", DevCoreArgumentTypes.columnBlockPosition()) {
    executes { context ->
      val resolver = context.getArgument("col", ColumnBlockPositionResolver::class.java)
      val col = resolver.resolve(context.source)
      val pos = col.toPosition(64) // Y座標を手動で指定
      context.sendSuccess { text("列: ${pos.blockX()}, ${pos.blockZ()}") }
      1
    }
  }
}
```

#### スコアボードチーム (team)

```kotlin
plugin.command("teaminfo") {
  argument("team", DevCoreArgumentTypes.team()) {
    executes { context ->
      val team = context.getArgument("team", Team::class.java)
      context.sendSuccess { text("チーム: ${team?.name}") }
      1
    }
  }
}
```

#### スコアボードオブジェクティブ (objective)

```kotlin
plugin.command("objectiveinfo") {
  argument("obj", DevCoreArgumentTypes.objective()) {
    executes { context ->
      val obj = context.getArgument("obj", Objective::class.java)
      context.sendSuccess { text("オブジェクティブ: ${obj?.name}") }
      1
    }
  }
}
```

#### インベントリスロット (slot / slots)

```kotlin
plugin.command("slot") {
  argument("slot", DevCoreArgumentTypes.slot()) {
    executes { context ->
      val slot = context.getArgument("slot", Int::class.java)
      context.sendSuccess { text("スロット番号: $slot") }
      1
    }
  }
}

plugin.command("slots") {
  argument("range", DevCoreArgumentTypes.slots()) {
    executes { context ->
      val range = context.getArgument("range", SlotRange::class.java)
      context.sendSuccess { text("スロット: ${range.slots} (${range.serializedName})") }
      1
    }
  }
}
```

#### ブロック述語 (blockInWorldPredicate)

プレイヤーの足元のブロックが条件に一致するか確認する例:

```kotlin
plugin.command("checkblock") {
  argument("filter", DevCoreArgumentTypes.blockInWorldPredicate()) {
    executes { context ->
      val predicate = context.getArgument("filter", BlockInWorldPredicate::class.java)
      val player = context.source.sender as? Player ?: return@executes 0
      val result = predicate.testBlock(player.location.block)
      context.sendSuccess { text("一致: ${result.asBoolean()}") }
      1
    }
  }
}
```

#### 軸セット (axes)

```kotlin
plugin.command("axes") {
  argument("axes", DevCoreArgumentTypes.axes()) {
    executes { context ->
      val axes = context.getArgument("axes", AxisSet::class.java)
      context.sendSuccess { text("軸: $axes") }
      1
    }
  }
}
```

#### 16進数カラー (hexColor)

```kotlin
plugin.command("color") {
  argument("color", DevCoreArgumentTypes.hexColor()) {
    executes { context ->
      val color = context.getArgument("color", TextColor::class.java)
      context.source.sender.sendMessage(
        Component.text("あなたの色はこれです！").color(color)
      )
      1
    }
  }
}
```

---

### アクセス制御

#### パーミッション

```kotlin
plugin.command("admin") {
  permission("myplugin.admin")
  executes { 1 }
}
```

#### OP限定

```kotlin
plugin.command("oponly") {
  requireOp()
  executes { 1 }
}
```

#### カスタム条件

```kotlin
plugin.command("custom") {
  requires { it.sender is Player && (it.sender as Player).health > 10.0 }
  executes { 1 }
}
```

---

### 実行対象の限定

#### プレイヤーのみ

```kotlin
plugin.command("playeronly") {
  executesPlayer { player, context ->
    player.sendMessage("あなたはプレイヤーです！")
    1
  }
}
```

#### コンソールのみ

```kotlin
plugin.command("consoleonly") {
  executesConsole { console, context ->
    console.sendMessage("コンソールから実行されました。")
    1
  }
}
```

---

### サジェスチョン（タブ補完）

#### 静的リスト

```kotlin
plugin.command("fruit") {
  string("name") {
    suggestion(listOf("apple", "banana", "orange"))
    executes { 1 }
  }
}
```

#### Enum値

```kotlin
plugin.command("gamemode") {
  word("mode") {
    suggestion<GameMode>()
    executes { 1 }
  }
}
```

#### 動的（非同期）サジェスチョン

```kotlin
plugin.command("warp") {
  word("name") {
    suggestionAsync { _ -> WarpManager.getWarpNames() }
    executes { 1 }
  }
}
```

---

### メッセージ送信

`executes` ブロック内で `sendSuccess`、`sendError`、`sendMessage` ヘルパーを使用できます。

```kotlin
executes { context ->
  context.sendSuccess { text("操作が完了しました。") }
  context.sendError { text("エラーが発生しました。") }
  context.sendMessage { text("中立メッセージ。") }
  1
}
```

#### ガード（条件付き実行）

```kotlin
executes { context ->
  context.guard(
    condition = someCondition,
    errorMessage = { text("条件を満たしていません！") }
  ) {
    // condition が true のときのみ実行される
    1
  }
}
```

---

### エイリアス

同じハンドラーを共有する複数のリテラル名を登録できます。

```kotlin
plugin.command("home") {
  aliases("h", "spawn") {
    executes { context ->
      context.sendSuccess { text("ホームに帰ります！") }
      1
    }
  }
}
```

---

### グローバルエラーハンドラーのカスタマイズ

デフォルトのエラーメッセージ形式をグローバルに上書きできます。

```kotlin
GlobalErrorHandler.updateErrorHandler { context, consumer ->
  context.source.sender.sendMessage(
    component {
      text("[エラー] ") { red(); bold() }
      create(consumer)
    }
  )
}
```
