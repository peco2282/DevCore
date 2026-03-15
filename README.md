# DevCore

[English] | [[日本語](README.ja.md)]

Core library for the DevCore project.

## Documents

- [Full Documentation](https://peco2282.github.io/DevCore/)

## Requirements

- JDK 21+

## Build

```bash
./gradlew build
```

## Publish

Set either Gradle properties or environment variables, then run `./gradlew publish`.

- Gradle properties: `devcore.publish.releaseUrl`, `devcore.publish.snapshotUrl`, `devcore.publish.user`,
  `devcore.publish.password`
- Environment variables: `DEVCORE_PUBLISH_RELEASE_URL`, `DEVCORE_PUBLISH_SNAPSHOT_URL`, `DEVCORE_PUBLISH_USER`,
  `DEVCORE_PUBLISH_PASSWORD`

## Usage (Gradle)

Repository:

```kotlin
repositories {
  maven("https://maven.peco2282.com/maven-releases/")
}
```

Consume the unified dependency:

```kotlin
dependencies {
  implementation("com.peco2282.devcore:core:1.0.0")
}
```

Or use the BOM and pick modules:

```kotlin
dependencies {
  implementation(platform("com.peco2282.devcore:devcore-bom:1.0.0"))
  implementation("com.peco2282.devcore:command")
  implementation("com.peco2282.devcore:config")
}
```

## Modules

Each module provides a specialized functionality and can be introduced individually or all at once through `core`. For more detailed information, please refer to the README in each module.

### adventure

Provides a Kotlin DSL for the Adventure library (KyoriPowered). Intuitive text construction and styling.
[Detailed documentation](adventure/README.md)

```kotlin
val msg = component {
  text("Hello ")
  text("World") { blue(); bold() }
}
```

### command

Define Paper (Brigadier) commands with a type-safe DSL. Define arguments, permissions, and suggestions concisely.
[Detailed documentation](command/README.md)

```kotlin
plugin.command("test") {
  player("target") {
    executes { context ->
      val target = context.getPlayer("target")
      context.sendSuccess { text("Hello, ${target?.name}") }
      1
    }
  }
}
```

### config

Automatically map YAML settings to Kotlin data classes. Supports validation via annotations and automatic insertion of comments.
[Detailed documentation](config/README.md)

```kotlin
@Comment("Main Config")
data class MyConfig(@Size(min = 1) @NotEmpty val levels: List<Int> = listOf(1, 2, 3))

val config = Configs.load<MyConfig>(plugin)
```

### scheduler

Thin wrapper for Bukkit scheduler. Provides tick-based time specification and task management tied to player/world lifecycles.
[Detailed documentation](scheduler/README.md)

```kotlin
plugin.taskCreate after 5.seconds run {
  println("Executed after 5 seconds")
}
player.taskTimer(plugin, 0.ticks, 20.ticks) {
  // Automatically cancelled on logout
}
```

### cooldown

General-purpose utility for managing cooldowns and debouncing (preventing rapid-fire) for players and the system as a whole.
[Detailed documentation](cooldown/README.md)

```kotlin
val cooldowns = PlayerCooldowns()
if (cooldowns.tryUse(player, 3.seconds)) {
  player.sendMessage("Skill used!")
}
```

### scoreboard

Simple DSL for creating sidebars and boss bars with automatic refresh and player-specific content.
[Detailed documentation](scoreboard/README.md)

```kotlin
val sidebar = sidebar(plugin, 20.ticks, component { text("Stats") }) {
  line { player -> component { text("Health: ${player.health.toInt()}") } }
  emptyLine()
  line(component { text("Server: devcore.com") })
}
sidebar.show(player)
```

### gui

Reactive GUI framework with DSL and State management. Supports dynamic title updates and pagination.
[Detailed documentation](gui/README.md)

```kotlin
val gui = inventory(rows = 3, title = component { text("Counter") }) {
  var count by state(0)
  slot(2, 5) {
    icon(Material.APPLE)
    name(component { text("Count: $count") })
    onClick { count++ }
  }
}
```

### packet

DSL for handling fake entities and packets using PacketEvents.
[Detailed documentation](packet/README.md)

```kotlin
player.sendFakeVisuals {
  spawnEntity(EntityType.ZOMBIE, location) {
    customName = "Fake Boss"
    isGlowing = true
  }
}
```

### event

DSL for defining Bukkit events concisely and type-safely.
[Detailed documentation](event/README.md)

```kotlin
on<PlayerJoinEvent> {
  handle { player.sendMessage("Welcome!") }
}
```

### effect

Utility for particle effects and visual enhancements.
[Detailed documentation](effect/README.md)

```kotlin
Effects.spawnCloud(location)
```

### core

Umbrella artifact to use all modules at once.
[Detailed documentation](core/README.md)

### bom

Bill of Materials to align versions across all modules.
[Detailed documentation](bom/README.md)

## License

Apache License 2.0. See [`LICENSE`](LICENSE).
