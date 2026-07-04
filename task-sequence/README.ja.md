# DevCore Task Sequence

[[English](README.md)] | [日本語]

`task-sequence` モジュールは、Kotlin Coroutines を活用して時系列に沿った演出（シーケンス）を記述するための機能を提供します。「Xティック待機してからYを実行する」といった複雑な演出の流れを、コールバックのネストなしに直線的で読みやすく記述できます。

## 特徴

- Coroutines を使用した直線的な時系列タスクの記述
- 直感的な `wait(ticks)` 関数による待機処理
- `Plugin`, `Scheduler`, `Player` に対する拡張関数による簡単なエントリポイント
- `Player.sequence` を使用した場合、プレイヤーの離脱に合わせて自動的にキャンセル
- `sync` / `async` による実行コンテキスト（スレッド）の切り替え
- `repeatWhile` / `repeatUntil` による条件付きループ
- `runIf` / `runUnless` による条件付き実行

## インストール (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:task-sequence:<version>")
}
```

## 使い方

### 基本的なシーケンス

```kotlin
plugin.sequence {
  player.sendMessage("シーケンス開始...")
  wait(20.ticks) // 1秒待機
  player.sendMessage("1秒経過しました！")
  wait(2.seconds) // 2秒待機
  player.sendMessage("完了！")
}
```

### プレイヤーに紐付いたシーケンス

`player.sequence` を使用すると、プレイヤーがサーバーを離脱した際にシーケンスが自動的にキャンセルされます。

```kotlin
player.sequence(plugin) {
  player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
  wait(10.ticks)
  player.spawnParticle(Particle.HAPPY_VILLAGER, player.location, 10)
}
```

### 高度な使い方

シーケンス内では、制御フローやコンテキストの切り替えが可能です。

#### 条件付き実行とループ

```kotlin
player.sequence(plugin) {
  repeatUntil({ player.isSneaking }) {
    player.sendMessage("スニークして停止してください...")
    wait(20.ticks)
  }
  
  runIf({ player.isOp }) {
    player.sendMessage("管理者として実行中...")
  }
  
  yield() // 1ティック待機
}
```

#### スレッドの切り替え

`sync` (メインスレッド) と `async` (非同期スレッド) を明示的に使い分けることができます。

```kotlin
plugin.sequence {
  val data = async {
    // 重い計算やデータベースアクセス
    "計算結果"
  }
  
  sync {
    // メインスレッドでのみ安全な Bukkit API の操作
    player.sendMessage(data)
  }
}
```
