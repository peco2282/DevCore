# DevCore Command
[English] | [[日本語](README.ja.md)]

Module for defining Paper (Brigadier) commands using a Kotlin DSL.

## Features

- Type-safe DSL based on Brigadier
- Intuitive argument definitions (string, integer, player, etc.)
- Condition-based execution restrictions (permission, op, etc.)
- Advanced suggestions (completion) functionality
- Targeted execution (executesPlayer, executesConsole)

## Install (Gradle Kotlin DSL)
```kotlin
dependencies {
  implementation("com.peco2282.devcore:command:<version>")
  // or:
  // implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  // implementation("com.peco2282.devcore:command")
}
```

## Usage

### Basic Command Definition

```kotlin
plugin.command("test") {
  requires { it.sender.isOp }
  
  literal("hello") {
    executes { context ->
      context.source.sender.sendMessage("Hello World!")
      1
    }
  }
}
```

### Using Arguments

```kotlin
plugin.command("givemoney") {
  permission("plugin.admin")
  
  player("target") {
    integer("amount", min = 1) {
      executes { context ->
        val target = context.getPlayer("target") ?: return@executes 0
        val amount = context.getArg<Int>("amount")
        // Process...
        1
      }
    }
  }
}
```

### Targeted Execution

```kotlin
plugin.command("playeronly") {
  executesPlayer { player, context ->
    player.sendMessage("You are a player!")
    1
  }
}
```

### Suggestions (Completion)

```kotlin
plugin.command("select") {
  string("name") {
    suggestion(listOf("apple", "banana", "orange"))
    executes { 1 }
  }
}
```

### Available Argument Types
- `string`, `word`, `greedyString`
- `integer`, `long`, `float`, `double`, `boolean`
- `player`, `players`, `entity`, `entities`
- `world`, `blockPos`, `finePos`, `rotation`

### Sending Messages
You can send messages using Adventure components directly within the DSL.

```kotlin
executes { context ->
  context.sendSuccess { text("Completed") }
  context.sendError { text("An error occurred") }
  1
}
```

