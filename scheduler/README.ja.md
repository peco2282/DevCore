# DevCore Scheduler
[[English](README.md)] | [日本語]

Bukkit scheduler を扱うための Kotlin DSL と、自動的なライフサイクル管理機能を提供します。

## 特徴

- 直感的なDSLによるタスクのスケジューリング
- `Ticks`クラスによる直感的な時間指定（ticks, seconds, minutes等）
- プレイヤーやワールドのライフサイクルに紐づいた自動キャンセル機能
- 同期/非同期実行の簡単な切り替え

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:scheduler:<version>")
  // または:
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  // implementation("com.peco2282.devcore:scheduler")
}
```

## 使用方法

### 基本的なタスク

```kotlin
// 遅延実行
plugin.taskCreate after 5.seconds run {
  println("5秒後に実行")
}

// 繰り返し実行
plugin.taskCreate after 0.ticks every 20.ticks run {
  println("1秒ごとに実行")
}

// 即時実行
plugin.taskCreate now {
  println("即時実行")
}

// 非同期実行
plugin.taskCreate async {
  println("非同期で実行")
}
```

### ライフサイクル紐付けタスク

プレイヤーのログアウトや、ワールドのアンロード時に自動でキャンセルされるタスクを作成できます。

```kotlin
// プレイヤーがログアウトするまで継続する遅延タスク
player.taskAfter(plugin, 10.seconds) {
  player.sendMessage("10秒後、まだログインしていればメッセージを表示")
}

// ワールドが存続している間のみ繰り返すタスク
world.taskTimer(plugin, 0.ticks, 20.ticks) {
  // ...
}
```

### 時間の指定 (`Ticks`)

`Int`拡張関数を使用して、直感的に時間を指定できます。

- `20.ticks` (1秒相当)
- `1.seconds`
- `5.minutes`
- `1.hours`
