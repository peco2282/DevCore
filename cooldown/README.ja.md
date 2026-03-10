# DevCore Cooldown
[[English](README.md)] | [日本語]

クールダウンおよびデバウンス（連打防止）を管理するための汎用ユーティリティです。
Kotlinの `Duration` をネイティブにサポートしています。

## 特徴

- シンプルなクールダウン管理（`Cooldowns`）
- 一定間隔での実行を保証するデバウンス（`Debounce`）
- 任意のキー型（UUID, String等）に対応
- Bukkit プレイヤー向けの便利なエイリアスと拡張関数

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:cooldown:<version>")
  // または:
  // implementation(platform("com.peco2282.devcore:bom:<version>"))
  // implementation("com.peco2282.devcore:cooldown")
}
```

## 使用方法

### プレイヤーのクールダウン管理

`PlayerCooldowns`（`Cooldowns<UUID>`のエイリアス）を使用して、プレイヤーごとのクールダウンを簡単に管理できます。

```kotlin
import com.peco2282.devcore.cooldown.PlayerCooldowns
import com.peco2282.devcore.cooldown.tryUse
import kotlin.time.Duration.Companion.seconds

val cooldowns = PlayerCooldowns()

fun onInteract(player: Player) {
  // 3秒のクールダウンをチェックし、可能であれば使用（更新）する
  if (cooldowns.tryUse(player, 3.seconds)) {
    player.sendMessage("スキルを使用しました！")
  } else {
    val remaining = cooldowns.remainingMillis(player.uniqueId) / 1000.0
    player.sendMessage("あと ${String.format("%.1f", remaining)} 秒待ってください。")
  }
}
```

### デバウンス（連打防止）

`Debounce`を使用すると、アクションが一定間隔より頻繁に発生しないように制御できます。

```kotlin
import com.peco2282.devcore.cooldown.PlayerDebounce
import com.peco2282.devcore.cooldown.allowEvery
import kotlin.time.Duration.Companion.milliseconds

val debounce = PlayerDebounce()

fun onChat(player: Player) {
  // 500ミリ秒に1回のみ許可
  if (debounce.allowEvery(player, 500.milliseconds)) {
    // チャット処理...
  }
}
```

### 汎用的な使用

任意のキー型を使用して、システム全般のクールダウンを管理できます。

```kotlin
val systemCooldown = Cooldowns<String>()

if (systemCooldown.tryUse("global-broadcast", 1.minutes)) {
  // 放送処理
}
```
