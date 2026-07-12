# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build all modules
./gradlew build

# Run tests across all modules
./gradlew test

# Run a single module's tests
./gradlew :command:test

# Launch a Paper 1.21.4 test server with TestPlugin
./gradlew :TestPlugin:runServer

# Generate multi-module Dokka documentation
./gradlew dokkaGenerate

# Publish all modules to Maven (skips already-published versions automatically)
./gradlew publish
```

Publishing requires credentials in `secrets.properties` or environment variables (`DEVCORE_PUBLISH_RELEASE_URL`, `DEVCORE_PUBLISH_SNAPSHOT_URL`, `DEVCORE_PUBLISH_USER`, `DEVCORE_PUBLISH_PASSWORD`).

## Architecture

DevCore is a multi-module Kotlin library for building **Paper Minecraft plugins**, exposing each feature domain as its own independently versioned Gradle module. Consumers use either `:core` (umbrella) or individual modules via `:bom` for version alignment.

### Module Layout

```
root
├── bom/              # Bill of Materials — version alignment only, not published as code
├── core/             # Umbrella artifact re-exporting key modules
├── util/             # Coroutines helpers, version detection, casting
├── command/          # Brigadier command DSL + Paper argument types
│   ├── v1_20_6/      # Version-specific NMS argument implementations
│   └── v1_21_1/
├── event/            # Bukkit event registration DSL
├── gui/              # Reactive inventory GUI with state management
├── config/           # YAML ↔ Kotlin data class mapping with validation annotations
├── scheduler/        # Task scheduling DSL (after/every/ticks/seconds)
├── adventure/        # Adventure text component DSL
├── packet/           # PacketEvents fake-entity/fake-block DSL
│   ├── v1_20_6/
│   ├── v1_21_4/
│   └── v1_21_11/
├── entity/           # Entity spawning and lifecycle management
├── scoreboard/
│   ├── scoreboard-api/
│   ├── scoreboard-lite/
│   └── scoreboard-nms/v1_21_4/
├── world/            # Block/world editing DSL
├── cooldown/         # Generic cooldown/debounce utility
├── task-sequence/    # Coroutine-based event sequencing
├── effect/           # Particle/visual effects
├── i18n/             # Internationalization
├── database/         # Multi-backend DB (api, jdbc, hikari, redis, mongodb, mysql, sqlite)
└── TestPlugin/       # Example plugin used to test all modules (not published)
```

### Versioning

Each module has its own version tracked in `gradle.properties` (e.g., `devcore.command.version=1.2.0`). Bump only the affected module's version on change. `devcore.dev=true` in `gradle.properties` indicates development/snapshot mode.

NMS-dependent modules split into a stable API module and per-Minecraft-version submodules (`v1_XX_X/`). The API module detects the server version at runtime and delegates accordingly.

### Convention Plugins (`buildSrc/`)

All library modules use these Gradle convention plugins:
- `devcore.kotlin-conventions` — JVM 21, Kotlin 2.1, JUnit Platform
- `devcore.dokka-conventions` — KDoc documentation generation
- `devcore.publish-conventions` — Maven publishing with idempotent version checks (skips already-published artifacts)

`TestPlugin` and `bom` are excluded from publish conventions. NMS submodules (`v1_XX_X`) are excluded from publishing and Dokka.

### Design Pattern: Plugin Extension DSL

Every module exposes its API through extension functions on `Plugin` (or related Bukkit types). This is the primary pattern throughout the codebase:

```kotlin
// Command registration
plugin.command("example") {
  literal("sub") {
    player("target") { executes { ctx -> /* ... */ } }
  }
}

// Event registration
plugin.on<PlayerJoinEvent> {
  filter { player.isOp }
  handle { player.sendMessage(text("Hello")) }
}

// GUI
plugin.openGui(player) {
  var count by state(0)
  slot(4) { icon(Material.DIAMOND); onClick { count++ } }
}

// Scheduling
plugin.taskCreate after 5.seconds run { /* one-shot */ }
plugin.taskCreate after 0.ticks every 20.ticks run { /* repeating */ }
```

### Command System (`command/`)

`CommandCreator<T>` wraps Brigadier's `ArgumentBuilder<CommandSourceStack, T>`. The entry point is `Plugin.command()` in `dsl.kt`, which registers via Paper's `LifecycleEvents.COMMANDS`.

Argument retrieval from `CommandContext` uses typed extension functions (`getPlayer()`, `getLocation()`, `getArg<T>()`, etc.) defined in `dsl.kt`. Custom Brigadier argument types implement `ArgumentType<T>` and are wired per MC version in `v1_XX_X/` submodules.

### GUI System (`gui/`)

State management uses Kotlin property delegates (`state<T>(default)`). Mutating a state variable automatically triggers an inventory re-render. `GuiListener` is a singleton that must be registered via `GuiListener.register(plugin)` in `onEnable` for click handling to work.

### Config System (`config/`)

Data classes annotated with `@Comment`, `@NotBlank`, `@Range`, `@Size`, `@Email`, etc. are mapped to/from YAML via `ClassMapper`. Load with `Configs.load<MyConfig>(plugin)`.

### Lifecycle-Aware Resources

Tasks and listeners can be bound to entity/world lifecycles so they cancel automatically:

```kotlin
player.taskAfter(plugin, 10.seconds) { /* cancels on logout */ }
location.world.taskTimer(plugin, 20.ticks, 20.ticks) { /* cancels on world unload */ }
```

### TestPlugin

The `TestPlugin` module (`com.peco2282.testplugin.TestPlugin`) is the integration harness — it imports all modules and exercises their APIs. Check it first when adding new features to see the expected usage pattern. Run it with `./gradlew :TestPlugin:runServer`.
