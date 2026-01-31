# DevCore Cooldown

クールダウン/デバウンスの汎用ユーティリティです（`Duration` 対応）。

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:cooldown:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:bom:<version>"))
  // implementation("com.peco2282.devcore:cooldown")
}
```

## Usage
```kotlin
import com.peco2282.devcore.cooldown.PlayerCooldowns
import kotlin.time.Duration.Companion.seconds

val cooldowns = PlayerCooldowns()
if (cooldowns.tryUse(player, 3.seconds)) {
  // do something
}
```

