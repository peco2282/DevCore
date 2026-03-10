# DevCore Scheduler
[English] | [[日本語](README.ja.md)]

Provides a Kotlin DSL for handling the Bukkit scheduler and automatic lifecycle management.

## Features

- Intuitive task scheduling using a DSL
- Intuitive time specification using the `Ticks` class (ticks, seconds, minutes, etc.)
- Automatic cancellation functionality tied to player and world lifecycles
- Easy switching between synchronous and asynchronous execution

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:scheduler:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  // implementation("com.peco2282.devcore:scheduler")
}
```

## Usage

### Basic Tasks

```kotlin
// Delayed execution
plugin.taskCreate after 5.seconds run {
  println("Executed after 5 seconds")
}

// Repeated execution
plugin.taskCreate after 0.ticks every 20.ticks run {
  println("Executed every second")
}

// Immediate execution
plugin.taskCreate now {
  println("Executed immediately")
}

// Asynchronous execution
plugin.taskCreate async {
  println("Executed asynchronously")
}
```

### Lifecycle-tied Tasks

Tasks can be created that are automatically cancelled when a player logs out or a world is unloaded.

```kotlin
// Delayed task that continues until the player logs out
player.taskAfter(plugin, 10.seconds) {
  player.sendMessage("Display message after 10 seconds if still logged in")
}

// Task that repeats only while the world exists
world.taskTimer(plugin, 0.ticks, 20.ticks) {
  // ...
}
```

### Specifying Time (`Ticks`)

You can intuitively specify time using `Int` extension functions.

- `20.ticks` (equivalent to 1 second)
- `1.seconds`
- `5.minutes`
- `1.hours`

