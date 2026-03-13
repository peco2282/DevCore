# scoreboard

A module for managing dynamic scoreboards (sidebars) and boss bars.

## Features

- **Concise Syntax with DSL**: Intuitive sidebar and boss bar construction using Kotlin DSL.
- **Packet-Based Sidebar**: Designed to minimize conflicts with other plugins' scoreboards.
- **Auto-Refresh Support**: Seamlessly updates content at specified intervals in collaboration with the `scheduler` module.
- **Per-Player Customization**: Dynamically modify line content and visibility conditions for each player.

## Installation

Add the following to your `build.gradle.kts`:

```kotlin
dependencies {
  implementation("com.peco2282.devcore:scoreboard")
}
```

## Sidebar

### Basic Usage

```kotlin
val sidebar = sidebar(component { text("Status") }) {
    line(component { text("Welcome!") })
    line { player -> component { text("Player: ${player.name}") } }
    emptyLine()
    line(component { text("Server: example.com") })
}

// Show to a player
sidebar.show(player)
```

### Enabling Auto-Refresh

You can automatically refresh the content by specifying a `plugin` and `interval`.

```kotlin
val sidebar = sidebar(plugin, 20.ticks, component { text("Dynamic Sidebar") }) {
    line { player -> 
        component { text("Time: ${System.currentTimeMillis()}") } 
    }
}
```

### Defining Multiple Lines (LinesBuilder)

The `lines` block allows for more flexible line additions.

```kotlin
sidebar(component { text("List") }) {
    lines {
        +"Static Text"
        +component { text("Adventure Component").gold() }
        +{ player: Player -> "Player HP: ${player.health}" }
    }
}
```

## BossBar

### Basic Usage

```kotlin
val bar = bossBar {
    title(component { text("Event in progress") })
    progress(0.5f)
    red() // Set color
    overlay(BossBar.Overlay.NOTCHED_10)
}

bar.show(player)
```

### Dynamic BossBar

```kotlin
val bar = bossBar(plugin) {
    title { player -> component { text("Your HP: ${player.health.toInt()}") } }
    progress { player -> (player.health / 20.0).toFloat() }
    green()
    autoRefresh(plugin, 10.ticks)
    
    // Set visibility condition
    filter { player -> player.world.name == "world" }
}
```

## Notes

- The `scheduler` module is required for updating sidebars and boss bars.
- When using the packet-based sidebar, the appropriate `scoreboard-nms` for your server version must be correctly loaded.
