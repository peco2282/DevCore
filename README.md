# DevCore

[English] | [[日本語](README.ja.md)]

Core library for the DevCore project.

## Documents

- [Documentation](https://peco2282.github.io/DevCore/)

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

Each module can be introduced individually or all at once through `core`.

- [**adventure**](#adventure): DSL for making the Adventure library easy to use from Kotlin. Intuitive text construction
  and styling.
- [**command**](#command): Define Paper (Brigadier) commands with a type-safe DSL. Define arguments, permissions, and
  suggestions concisely.
- [**config**](#config): Automatically map YAML settings to Kotlin data classes. Supports validation via annotations and
  automatic insertion of comments.
- [**scheduler**](#scheduler): Thin wrapper for Bukkit scheduler. Provides tick-based time specification and task
  management tied to player/world lifecycles.
- [**cooldown**](#cooldown): General-purpose utility for managing cooldowns and debouncing (preventing rapid-fire) for
  players and the system as a whole.
- [**gui**](#gui): Reactive GUI framework with DSL and State management. Supports dynamic title updates and pagination.
- [**scoreboard**](#scoreboard): DSL for dynamic Scoreboard/BossBar management. Packet-based and dynamic updates via
  Scheduler.
- [**core**](#core): Umbrella artifact to use all modules at once.

---

### adventure

[Link to module](adventure/README.md)

```kotlin
val msg = component {
  text("Hello ")
  text("World") { blue(); bold() }
}
```

### command

[Link to module](command/README.md)

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

[Link to module](config/README.md)

```kotlin
@Comment("Main Config")
data class MyConfig(@Size(min = 1) @NotEmpty val levels: List<Int> = listOf(1, 2, 3))

val config = Configs.load<MyConfig>(plugin)
```

### scheduler

[Link to module](scheduler/README.md)

```kotlin
plugin.taskCreate after 5.seconds run {
  println("Executed after 5 seconds")
}
player.taskTimer(plugin, 0.ticks, 20.ticks) {
  // Automatically cancelled on logout
}
```

### cooldown

[Link to module](cooldown/README.md)

```kotlin
val cooldowns = PlayerCooldowns()
if (cooldowns.tryUse(player, 3.seconds)) {
  player.sendMessage("Skill used!")
}
```

### scoreboard

[Link to module](scoreboard/README.md)

```kotlin
val sidebar = sidebar(plugin, 20.ticks, component { text("Stats") }) {
  line { player -> component { text("Health: ${player.health.toInt()}") } }
  emptyLine()
  line(component { text("Server: devcore.com") })
}
sidebar.show(player)

val bar = bossBar(plugin) {
  title { player -> component { text("HP: ${player.health.toInt()}") } }
  progress { player -> (player.health / 20.0).toFloat() }
  red()
  autoRefresh(plugin, 20.ticks)
}
bar.show(player)
```

### gui

[Link to module](gui/README.md)

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

### core

[Link to module](core/README.md)
Umbrella artifact to use all modules.

## License

Apache License 2.0. See [`LICENSE`](LICENSE).
