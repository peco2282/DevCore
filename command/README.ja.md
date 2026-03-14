# DevCore Command

[[English](README.md)] | [日本語]

Paper (Brigadier) コマンドを Kotlin DSL で定義するためのモジュールです。

## 特徴

- Brigadierをベースとした型安全なDSL
- 直感的な引数定義（string, integer, player等）
- 条件ベースの実行制限（permission, op等）
- 高度なサジェスチョン（補完）機能
- 実行対象の限定（executesPlayer, executesConsole）

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:command:<version>")
  // または:
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  // implementation("com.peco2282.devcore:command")
}
```

## 使用方法

### 基本的なコマンド定義

```kotlin
plugin.command("test") {
  requires { it.sender.isOp }
  
  literal("hello") {
    executes { context ->
      context.source.sender.sendMessage("Hello World!")
      1
    }
  }
}
```

### 引数の利用

```kotlin
plugin.command("givemoney") {
  permission("plugin.admin")
  
  player("target") {
    integer("amount", min = 1) {
      executes { context ->
        val target = context.getPlayer("target") ?: return@executes 0
        val amount = context.getArg<Int>("amount")
        // 処理...
        1
      }
    }
  }
}
```

### 実行対象の限定

```kotlin
plugin.command("playeronly") {
  executesPlayer { player, context ->
    player.sendMessage("You are a player!")
    1
  }
}
```

### サジェスチョン（補完）

```kotlin
plugin.command("select") {
  string("name") {
    suggestion(listOf("apple", "banana", "orange"))
    executes { 1 }
  }
}
```

### 利用可能な引数タイプ

- `string`, `word`, `greedyString`
- `integer`, `long`, `float`, `double`, `boolean`
- `player`, `players`, `entity`, `entities`
- `world`, `blockPos`, `finePos`, `rotation`

### メッセージ送信

DSL内からAdventureコンポーネントを直接使用してメッセージを送信できます。

```kotlin
executes { context ->
  context.sendSuccess { text("完了しました") }
  context.sendError { text("エラーが発生しました") }
  1
}
```
