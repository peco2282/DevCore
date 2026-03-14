# gui

[English] | [[日本語](README.ja.md)]

Reactive GUI framework for Bukkit/Spigot with Kotlin DSL and State management.

## Features

- **Declarative DSL**: Build GUIs intuitively using Kotlin's type-safe builders.
- **Reactive State Management**: Automatically updates the GUI when states change.
- **Dynamic Title**: Automatically handles title updates by recreating the inventory and re-opening it for viewers.
- **Pagination Support**: Easily create paginated lists with built-in `PaginatedGuiContext`.

## Usage

### Simple GUI

```kotlin
val gui = inventory(rows = 3, title = component { text("My GUI") }) {
  slot(1, 1) {
    icon(Material.DIAMOND)
    name(component { text("Click Me") })
    onClick {
      player.sendMessage("You clicked the diamond!")
    }
  }
}
gui.open(player)
```

### State Management

```kotlin
val gui = inventory(rows = 3, title = component { text("Counter") }) {
  var count by state(0)

  slot(2, 5) {
    icon(Material.APPLE)
    name(component { text("Count: $count") })
    onClick {
      count++ // GUI automatically updates!
    }
  }
}
```

### Pagination

```kotlin
val items = (1..100).map { "Item #$it" }
val gui = paginatedInventory(rows = 6, title = component { text("Items") }, items = items) {
  // Define content area
  content(9..44) { item ->
    icon(Material.PAPER)
    name(component { text(item) })
  }

  // Previous page
  slot(6, 1) {
    icon(Material.ARROW)
    name(component { text("Previous") })
    onClick { prevPage() }
  }

  // Next page
  slot(6, 9) {
    icon(Material.ARROW)
    name(component { text("Next") })
    onClick { nextPage() }
  }
}
```

## Setup

```kotlin
// Register the listener once in your plugin's onEnable
GuiListener.register(plugin)
```
