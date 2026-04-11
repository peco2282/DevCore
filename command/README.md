# DevCore Command

[English] | [[日本語](README.ja.md)]

Module for defining Paper (Brigadier) commands using a Kotlin DSL.

## Features

- Type-safe DSL based on Brigadier
- Intuitive argument definitions (string, integer, player, etc.)
- Condition-based execution restrictions (permission, op, etc.)
- Advanced suggestions (tab-completion) functionality
- Targeted execution (executesPlayer, executesConsole)
- Version-transparent NMS argument types via `DevCoreArgumentTypes`

## Install (Gradle Kotlin DSL)

```kotlin
repositories {
  maven("https://maven.peco2282.com/maven-releases/")
}

dependencies {
  implementation("com.peco2282.devcore:command:<version>")
  // or use the BOM:
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  // implementation("com.peco2282.devcore:command")
}
```

---

## Usage

### Registering a Command

Use the `plugin.command(name) { ... }` extension function to define and register a command.
The DSL block receives a `CommandCreator` where you configure the entire command tree.

```kotlin
plugin.command("hello") {
  executes { context ->
    context.source.sender.sendMessage("Hello, World!")
    1
  }
}
```

---

### Literal Subcommands

Use `literal` (or the `"name" { }` operator shorthand) to add subcommands:

```kotlin
plugin.command("admin") {
  requireOp()

  literal("reload") {
    executes { context ->
      context.sendSuccess { text("Config reloaded.") }
      1
    }
  }

  "info" {
    executes { context ->
      context.sendSuccess { text("Server info here.") }
      1
    }
  }
}
```

---

### Arguments

#### Built-in DSL argument helpers

The following argument helpers are available directly in the DSL:

| Method | Type | Description |
|---|---|---|
| `string(name)` | `String` | A quoted or unquoted string |
| `word(name)` | `String` | A single word (no spaces) |
| `greedyString(name)` | `String` | All remaining input as one string |
| `integer(name, min, max)` | `Int` | Integer with optional range |
| `long(name, min, max)` | `Long` | Long with optional range |
| `float(name, min, max)` | `Float` | Float with optional range |
| `double(name, min, max)` | `Double` | Double with optional range |
| `boolean(name)` | `Boolean` | `true` or `false` |
| `player(name)` | `PlayerSelectorArgumentResolver` | Single player selector |
| `players(name)` | `PlayerSelectorArgumentResolver` | Multiple player selector |
| `entity(name)` | `EntitySelectorArgumentResolver` | Single entity selector |
| `entities(name)` | `EntitySelectorArgumentResolver` | Multiple entity selector |
| `world(name)` | `World` | A loaded world |
| `blockPos(name)` | `BlockPositionResolver` | Integer block coordinates (X Y Z) |
| `finePos(name)` | `FinePositionResolver` | Decimal coordinates (X Y Z) |
| `rotation(name)` | `RotationResolver` | Yaw and pitch angles |

```kotlin
plugin.command("givemoney") {
  permission("myplugin.admin")

  player("target") {
    integer("amount", min = 1) {
      executes { context ->
        val players = context.getArgument("target", PlayerSelectorArgumentResolver::class.java)
          .resolve(context.source)
        val amount = context.getArgument("amount", Int::class.java)
        players.forEach { it.sendMessage("You received $$amount!") }
        1
      }
    }
  }
}
```

---

### DevCoreArgumentTypes

`DevCoreArgumentTypes` provides additional Minecraft/NMS-backed argument types that are not
available as DSL helpers. It automatically selects the correct version-specific implementation
at runtime (supports `1.20.x` and `1.21+`).

Use it with the generic `argument(name, type) { }` builder:

```kotlin
import com.peco2282.devcore.command.argument.DevCoreArgumentTypes
import com.peco2282.devcore.command.argument.FinePositionResolver
import com.peco2282.devcore.command.argument.RotationResolver
import com.peco2282.devcore.command.argument.ColumnBlockPositionResolver
```

#### Fine position (decimal coordinates)

```kotlin
plugin.command("tp") {
  argument("pos", DevCoreArgumentTypes.finePosition(centerIntegers = true)) {
    executes { context ->
      val resolver = context.getArgument("pos", FinePositionResolver::class.java)
      val pos = resolver.resolve(context.source)
      val player = context.source.sender as? Player ?: return@executes 0
      player.teleport(pos.toLocation(player.world))
      1
    }
  }
}
```

#### Rotation (yaw and pitch)

```kotlin
plugin.command("setrot") {
  argument("rot", DevCoreArgumentTypes.rotation()) {
    executes { context ->
      val resolver = context.getArgument("rot", RotationResolver::class.java)
      val rot = resolver.resolve(context.source)
      val player = context.source.sender as? Player ?: return@executes 0
      player.teleport(player.location.apply { yaw = rot.yaw; pitch = rot.pitch })
      1
    }
  }
}
```

#### Column block position (X Z only)

```kotlin
plugin.command("column") {
  argument("col", DevCoreArgumentTypes.columnBlockPosition()) {
    executes { context ->
      val resolver = context.getArgument("col", ColumnBlockPositionResolver::class.java)
      val col = resolver.resolve(context.source)
      val pos = col.toPosition(64) // supply Y manually
      context.sendSuccess { text("Column: ${pos.blockX()}, ${pos.blockZ()}") }
      1
    }
  }
}
```

#### Scoreboard team

```kotlin
plugin.command("teaminfo") {
  argument("team", DevCoreArgumentTypes.team()) {
    executes { context ->
      val team = context.getArgument("team", Team::class.java)
      context.sendSuccess { text("Team: ${team?.name}") }
      1
    }
  }
}
```

#### Scoreboard objective

```kotlin
plugin.command("objectiveinfo") {
  argument("obj", DevCoreArgumentTypes.objective()) {
    executes { context ->
      val obj = context.getArgument("obj", Objective::class.java)
      context.sendSuccess { text("Objective: ${obj?.name}") }
      1
     }
  }
}
```

#### Inventory slot / slots

```kotlin
plugin.command("slot") {
  argument("slot", DevCoreArgumentTypes.slot()) {
    executes { context ->
      val slot = context.getArgument("slot", Int::class.java)
      context.sendSuccess { text("Slot index: $slot") }
      1
    }
  }
}

plugin.command("slots") {
  argument("range", DevCoreArgumentTypes.slots()) {
    executes { context ->
      val range = context.getArgument("range", SlotRange::class.java)
      context.sendSuccess { text("Slots: ${range.slots} (${range.serializedName})") }
      1
    }
  }
}
```

#### Block predicate

```kotlin
plugin.command("checkblock") {
  argument("filter", DevCoreArgumentTypes.blockInWorldPredicate()) {
    executes { context ->
      val predicate = context.getArgument("filter", BlockInWorldPredicate::class.java)
      val player = context.source.sender as? Player ?: return@executes 0
      val result = predicate.testBlock(player.location.block)
      context.sendSuccess { text("Match: ${result.asBoolean()}") }
      1
    }
  }
}
```

#### Axes

```kotlin
plugin.command("axes") {
  argument("axes", DevCoreArgumentTypes.axes()) {
    executes { context ->
      val axes = context.getArgument("axes", AxisSet::class.java)
      context.sendSuccess { text("Axes: $axes") }
      1
    }
  }
}
```

#### Hex color

```kotlin
plugin.command("color") {
  argument("color", DevCoreArgumentTypes.hexColor()) {
    executes { context ->
      val color = context.getArgument("color", TextColor::class.java)
      context.source.sender.sendMessage(
        Component.text("This is your color!").color(color)
      )
      1
    }
  }
}
```

---

### Access Control

#### Permission

```kotlin
plugin.command("admin") {
  permission("myplugin.admin")
  executes { 1 }
}
```

#### Operator only

```kotlin
plugin.command("oponly") {
  requireOp()
  executes { 1 }
}
```

#### Custom condition

```kotlin
plugin.command("custom") {
  requires { it.sender is Player && (it.sender as Player).health > 10.0 }
  executes { 1 }
}
```

---

### Targeted Execution

#### Player only

```kotlin
plugin.command("playeronly") {
  executesPlayer { player, context ->
    player.sendMessage("You are a player!")
    1
  }
}
```

#### Console only

```kotlin
plugin.command("consoleonly") {
  executesConsole { console, context ->
    console.sendMessage("Running from console.")
    1
  }
}
```

---

### Suggestions (Tab Completion)

#### Static list

```kotlin
plugin.command("fruit") {
  string("name") {
    suggestion(listOf("apple", "banana", "orange"))
    executes { 1 }
  }
}
```

#### Enum values

```kotlin
plugin.command("gamemode") {
  word("mode") {
    suggestion<GameMode>()
    executes { 1 }
  }
}
```

#### Dynamic (async) suggestions

```kotlin
plugin.command("warp") {
  word("name") {
    suggestionAsync { _ -> WarpManager.getWarpNames() }
    executes { 1 }
  }
}
```

---

### Sending Messages

Use the `sendSuccess`, `sendError`, and `sendMessage` helpers inside `executes` blocks:

```kotlin
executes { context ->
  context.sendSuccess { text("Operation completed successfully.") }
  context.sendError { text("Something went wrong.") }
  context.sendMessage { text("Neutral message.") }
  1
}
```

#### Guard (conditional execution)

```kotlin
executes { context ->
  context.guard(
    condition = someCondition,
    errorMessage = { text("Condition not met!") }
  ) {
    // executed only when condition is true
    1
  }
}
```

---

### Aliases

Register multiple literal names that share the same handler:

```kotlin
plugin.command("home") {
  aliases("h", "spawn") {
    executes { context ->
      context.sendSuccess { text("Going home!") }
      1
    }
  }
}
```

---

### Custom Global Error Handler

Override the default error message format globally:

```kotlin
GlobalErrorHandler.updateErrorHandler { context, consumer ->
  context.source.sender.sendMessage(
    component {
      text("[ERROR] ") { red(); bold() }
      create(consumer)
    }
  )
}
```
