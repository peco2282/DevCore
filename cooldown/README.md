# DevCore Cooldown

[English] | [[日本語](README.ja.md)]

General-purpose utility for managing cooldowns and debouncing (rapid-fire prevention).
Native support for Kotlin's `Duration`.

## Features

- Simple cooldown management (`Cooldowns`)
- Debounce for ensuring execution at fixed intervals (`Debounce`)
- Support for any key type (UUID, String, etc.)
- Convenient aliases and extension functions for Bukkit players

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:cooldown:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  // implementation("com.peco2282.devcore:cooldown")
}
```

## Usage

### Player Cooldown Management

Using `PlayerCooldowns` (alias for `Cooldowns<UUID>`), you can easily manage cooldowns per player.

```kotlin
import com.peco2282.devcore.cooldown.PlayerCooldowns
import com.peco2282.devcore.cooldown.tryUse
import kotlin.time.Duration.Companion.seconds

val cooldowns = PlayerCooldowns()

fun onInteract(player: Player) {
  // Check for a 3-second cooldown and use (update) if possible
  if (cooldowns.tryUse(player, 3.seconds)) {
    player.sendMessage("Skill used!")
  } else {
    val remaining = cooldowns.remainingMillis(player.uniqueId) / 1000.0
    player.sendMessage("Please wait ${String.format("%.1f", remaining)} seconds.")
  }
}
```

### Debouncing (Rapid-fire prevention)

Using `Debounce`, you can control actions so they don't occur more frequently than a certain interval.

```kotlin
import com.peco2282.devcore.cooldown.PlayerDebounce
import com.peco2282.devcore.cooldown.allowEvery
import kotlin.time.Duration.Companion.milliseconds

val debounce = PlayerDebounce()

fun onChat(player: Player) {
  // Allow only once every 500 milliseconds
  if (debounce.allowEvery(player, 500.milliseconds)) {
    // Chat process...
  }
}
```

### General Usage

You can manage system-wide cooldowns using any key type.

```kotlin
val systemCooldown = Cooldowns<String>()

if (systemCooldown.tryUse("global-broadcast", 1.minutes)) {
  // Broadcast process...
}
```

