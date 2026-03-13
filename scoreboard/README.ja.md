# scoreboard

動的なスコアボード（サイドバー）とボスバーを管理するためのモジュールです。

## 特徴

- **DSLによる簡潔な記述**: KotlinのDSLを使用して、直感的にサイドバーやボスバーを構築できます。
- **パケットベースのサイドバー**: 他のプラグインのスコアボードと競合しにくい設計になっています。
- **自動更新サポート**: `scheduler` モジュールと連携し、指定した間隔で動的に内容を更新できます。
- **プレイヤーごとのカスタマイズ**: 行の内容や表示条件をプレイヤーごとに動的に変更可能です。

## 導入方法

`build.gradle.kts` に以下を追加してください。

```kotlin
dependencies {
  implementation("com.peco2282.devcore:scoreboard")
}
```

## サイドバー (Sidebar)

### 基本的な使い方

```kotlin
val sidebar = sidebar(component { text("ステータス") }) {
    line(component { text("こんにちは！") })
    line { player -> component { text("プレイヤー: ${player.name}") } }
    emptyLine()
    line(component { text("サーバー: example.com") })
}

// プレイヤーに表示
sidebar.show(player)
```

### 自動更新の有効化

`plugin` と `interval` を指定することで、内容を自動的に更新できます。

```kotlin
val sidebar = sidebar(plugin, 20.ticks, component { text("動的サイドバー") }) {
    line { player -> 
        component { text("現在時刻: ${System.currentTimeMillis()}") } 
    }
}
```

### 複数行の定義 (LinesBuilder)

`lines` ブロックを使用すると、より柔軟に行を追加できます。

```kotlin
sidebar(component { text("リスト") }) {
    lines {
        +"固定テキスト"
        +component { text("Adventureコンポーネント").gold() }
        +{ player: Player -> "プレイヤーHP: ${player.health}" }
    }
}
```

## ボスバー (BossBar)

### 基本的な使い方

```kotlin
val bar = bossBar {
    title(component { text("イベント進行中") })
    progress(0.5f)
    red() // 色の指定
    overlay(BossBar.Overlay.NOTCHED_10)
}

bar.show(player)
```

### 動的なボスバー

```kotlin
val bar = bossBar(plugin) {
    title { player -> component { text("あなたのHP: ${player.health.toInt()}") } }
    progress { player -> (player.health / 20.0).toFloat() }
    green()
    autoRefresh(plugin, 10.ticks)
    
    // 表示条件の指定
    filter { player -> player.world.name == "world" }
}
```

## 注意事項

- サイドバーやボスバーの更新には `scheduler` モジュールが必要です。
- パケットベースのサイドバーを使用する場合、サーバーのバージョンに対応した `scoreboard-nms` が適切にロードされている必要があります。
