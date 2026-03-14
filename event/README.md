# DevCore Event

[English] | [[日本語](README.ja.md)]

DSL for defining Bukkit events concisely and type-safely.

## Features

- Intuitive DSL for registering events
- Easy-to-use filters and conditions
- Automatic unregistration support
- Grouping multiple events

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:event:<version>")
}
```

## Usage

### Basic Event Listener

```kotlin
on<PlayerJoinEvent> {
  handle {
    player.sendMessage("Welcome!")
  }
}
```

### Advanced Usage

```kotlin
on<PlayerInteractEvent> {
  filter { action.isLeftClick }
  once() // Only triggered once
  handle {
    player.sendMessage("Left clicked!")
  }
}
```

### Event Groups

```kotlin
val group = events {
  on<PlayerJoinEvent> {
    handle { player.sendMessage("Hello!") }
  }
  on<PlayerQuitEvent> {
    handle { println("${player.name} left.") }
  }
}

// Later
group.unregisterAll()
```
