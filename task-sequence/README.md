# DevCore Task Sequence

[English] | [[日本語](README.ja.md)]

The `task-sequence` module provides a way to describe time-series performances (sequences) based on Kotlin Coroutines. It allows you to write complex演出 flows, such as "wait for X ticks, then do Y", in a linear and readable way without nested callbacks.

## Features

- Linear description of time-series tasks using Coroutines
- Intuitive `wait(ticks)` function for delays
- Extension functions for `Plugin`, `Scheduler`, and `Player` for easy entry
- Automatically tied to player lifecycle when using `Player.sequence`
- Execution context switching via `sync` / `async`
- Conditional loops with `repeatWhile` / `repeatUntil`
- Conditional execution with `runIf` / `runUnless`

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:task-sequence:<version>")
}
```

## Usage

### Basic Sequence

```kotlin
plugin.sequence {
  player.sendMessage("Starting sequence...")
  wait(20.ticks) // Wait for 1 second
  player.sendMessage("1 second passed!")
  wait(2.seconds) // Wait for 2 seconds
  player.sendMessage("Finished!")
}
```

### Player-tied Sequence

When using `player.sequence`, the sequence will be automatically cancelled if the player leaves the server.

```kotlin
player.sequence(plugin) {
  player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
  wait(10.ticks)
  player.spawnParticle(Particle.HAPPY_VILLAGER, player.location, 10)
}
```

### Advanced Usage

You can use control flows and context switching within the sequence.

#### Conditional Execution and Loops

```kotlin
player.sequence(plugin) {
  repeatUntil({ player.isSneaking }) {
    player.sendMessage("Please sneak to stop...")
    wait(20.ticks)
  }
  
  runIf({ player.isOp }) {
    player.sendMessage("Running as operator...")
  }
  
  yield() // Wait for 1 tick
}
```

#### Context Switching

You can switch between `sync` (Main Thread) and `async` (Asynchronous) contexts.

```kotlin
plugin.sequence {
  val data = async {
    // Heavy calculation or database access
    "Result"
  }
  
  sync {
    // Operations that require Bukkit API (Main Thread)
    player.sendMessage(data)
  }
}
```
