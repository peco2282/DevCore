# DevCore

[English] | [[日本語](README.ja.md)]

Core library for the DevCore project.

## Documents

- [Full Documentation](https://peco2282.github.io/DevCore/)

## Requirements

- JDK 21+
- Paper 1.20.6+ (version-specific modules document their supported releases)

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

## Before / After

DevCore replaces repetitive Paper/Bukkit code with short DSLs. The same behavior, written both ways (52 lines → 18 lines across these four examples):

### Events (OPs only, first 3 times)

**Before** — plain Paper / Bukkit API (11 lines)

```kotlin
class JoinListener : Listener {
  private var count = 0

  @EventHandler
  fun onJoin(event: PlayerJoinEvent) {
    if (!event.player.isOp) return
    event.player.sendMessage("Welcome, operator!")
    if (++count >= 3) HandlerList.unregisterAll(this)
  }
}

// onEnable
server.pluginManager.registerEvents(JoinListener(), this)
```

**After** — DevCore (5 lines)

```kotlin
plugin.on<PlayerJoinEvent> {
  filter { player.isOp }
  take(3)
  handle { player.sendMessage("Welcome, operator!") }
}
```

### Repeating task tied to a player (stops on logout)

**Before** — plain Paper / Bukkit API (11 lines)

```kotlin
val task = object : BukkitRunnable() {
  override fun run() {
    player.sendActionBar(Component.text("tick"))
  }
}.runTaskTimer(plugin, 0L, 20L)

server.pluginManager.registerEvents(object : Listener {
  @EventHandler
  fun onQuit(e: PlayerQuitEvent) {
    if (e.player == player) task.cancel()
  }
}, plugin)
```

**After** — DevCore (3 lines)

```kotlin
player.taskTimer(plugin, 0.ticks, 20.ticks) {
  player.sendActionBar(Component.text("tick"))
}
```

### A particle ring that follows a player for 5 seconds

**Before** — plain Paper / Bukkit API (16 lines)

```kotlin
object : BukkitRunnable() {
  var elapsed = 0

  override fun run() {
    if (!player.isValid || elapsed >= 100) {
      cancel()
      return
    }
    val center = player.location.add(0.0, 1.0, 0.0)
    for (i in 0 until 36) {
      val angle = 2 * Math.PI * i / 36
      val at = center.clone().add(cos(angle) * 1.5, 0.0, sin(angle) * 1.5)
      player.world.spawnParticle(Particle.END_ROD, at, 1)
    }
    elapsed += 2
  }
}.runTaskTimer(plugin, 0L, 2L)
```

**After** — DevCore (2 lines)

```kotlin
val ring = effect { particle(Particle.END_ROD, circle(1.5).offset(y = 1.0)) }
plugin.playEffect(ring, player, every = 2.ticks, duration = 5.seconds)
```

### Command with a player argument

**Before** — plain Paper / Bukkit API (14 lines)

```kotlin
lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
  event.registrar().register(
    Commands.literal("greet")
      .then(
        Commands.argument("target", ArgumentTypes.player())
          .executes { ctx ->
            val target = ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java)
              .resolve(ctx.source).first()
            ctx.source.sender.sendMessage("Hello, ${target.name}")
            Command.SINGLE_SUCCESS
          }
      ).build()
  )
}
```

**After** — DevCore (8 lines)

```kotlin
plugin.command("greet") {
  player("target") {
    executes { ctx ->
      ctx.sendSuccess { text("Hello, ${ctx.getPlayer("target")?.name}") }
      1
    }
  }
}
```

## Which module do I need?

| Module | Removes boilerplate around | In `core` |
|---|---|:---:|
| `adventure` | Building and styling Adventure components | ✅ |
| `command` | Brigadier command trees, arguments, suspend handlers | ✅ |
| `config` | YAML ↔ data class mapping, validation, comments | ✅ |
| `scheduler` | `BukkitRunnable`, lifecycle-bound tasks, coroutine dispatcher | ✅ |
| `task-sequence` | Chained "wait, then do" timelines | ✅ |
| `event` | `Listener` classes, `@EventHandler`, manual unregistering | ✅ |
| `cooldown` | Per-player cooldown / debounce maps | ✅ |
| `gui` | Inventory click handling and re-rendering | ✅ |
| `scoreboard` | Sidebars and boss bars (`scoreboard-lite` is in `core`) | ✅ (lite) |
| `effect` | Particle shape math, repeated/following playback | ✅ |
| `display` | Text/item/block displays and managed holograms | – |
| `region` | Geometric regions and enter/leave tracking | – |
| `teleport` | Safe destinations and cancellable warmups | – |
| `game` | Minigame state and participant lifecycle | – |
| `loot` | Weighted and conditional reward tables | – |
| `merchant` | Merchant trade DSL | – |
| `nametag` | Team nametags and tab-list content | – |
| `kit` | Permission/cooldown-aware item kits | – |
| `worldpool` | Disposable worlds copied from templates | – |
| `cache` | Thread-safe TTL caches | – |
| `permission` | Typed permission declarations | – |
| `debug` | Runtime debug categories and timing | – |
| `ranking` | Refreshed ranking snapshots | – |
| `persistent-cooldown` | Restart-resistant player cooldowns | – |
| `update-checker` | Opt-in asynchronous release checks | – |
| `anvil-input` | Version-adapted anvil text prompts | – |
| `packet` | Fake entities / blocks through PacketEvents | – |
| `entity` | Spawning, AI goals, lifecycle hooks | – |
| `world` | Block / chunk / player editing | – |
| `database` | Connection setup, migrations, repositories | – |
| `i18n` | Locale files, fallback, MiniMessage placeholders | – |
| `pdc` | `NamespacedKey` + `PersistentDataType` pairs, meta get/set round trips | – |
| `item` | Custom item ids, listeners, recipes | – |
| `storage` | Async per-player load/save, autosave, safe writes | – |
| `util` | Casting, version checks, small helpers | – |

Modules marked "–" are not pulled in by `core`; add them individually (versions are aligned by the BOM).

Module guides: [display](display/README.md), [region](region/README.md), [teleport](teleport/README.md),
[game](game/README.md), [loot](loot/README.md), [merchant](merchant/README.md), [nametag](nametag/README.md),
[kit](kit/README.md), [worldpool](worldpool/README.md), [cache](cache/README.md),
[permission](permission/README.md), [debug](debug/README.md), [ranking](ranking/README.md),
[persistent-cooldown](persistent-cooldown/README.md), [update-checker](update-checker/README.md), and
[anvil-input](anvil-input/README.md).

DevCore currently targets Paper. Folia compatibility is intentionally deferred and is not guaranteed.

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
  implementation("com.peco2282.devcore:core:1.2.0")
}
```

Or use the BOM and pick modules:

```kotlin
dependencies {
  implementation(platform("com.peco2282.devcore:devcore-bom:1.2.0"))
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

### task-sequence

Module for describing time-series performances (sequences). Based on coroutines, it allows for intuitive descriptions of sequences such as "play a sound after 10 ticks" and "an explosion effect 5 ticks after that".
[Detailed documentation](task-sequence/README.md)

```kotlin
plugin.sequence {
  player.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
  wait(10.ticks)
  location.spawnParticle(Particle.EXPLOSION, 10, 0.5, 0.5, 0.5)
  wait(5.ticks)
  location.createExplosion(2f)
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

### entity

Easy entity management, AI control, and lifecycle handling.
[Detailed documentation](entity/README.md)

```kotlin
location.spawn<Zombie> {
  isNoAi = true
  onDeath(plugin) {
    player.sendMessage("Died!")
  }
}
```

### world

Type-safe DSLs for editing worlds, chunks, blocks, and players.
[Detailed documentation](world/README.md)

### database

Database abstractions and DSL-based adapters for JDBC, HikariCP, MySQL, SQLite, Redis, and MongoDB.
[Detailed documentation](database/README.md)

### i18n

YAML-based localization for Bukkit plugins with locale fallback and message placeholders.
[Detailed documentation](i18n/README.md)

### event

DSL for defining Bukkit events concisely and type-safely.
[Detailed documentation](event/README.md)

```kotlin
on<PlayerJoinEvent> {
  handle { player.sendMessage("Welcome!") }
}
```

### effect

Reusable particle/sound effects with shape generators (circle, sphere, helix, line) and repeating or entity-following playback.
[Detailed documentation](effect/README.md)

```kotlin
val ring = effect {
  particle(Particle.END_ROD, circle(1.5).offset(y = 1.0))
  sound(Sound.ENTITY_PLAYER_LEVELUP)
}
plugin.playEffect(ring, player, every = 2.ticks, duration = 5.seconds)
```

### pdc

Typed `PersistentDataContainer` access and self-saving properties for players, entities, items and blocks.
[Detailed documentation](pdc/README.md)

```kotlin
var Player.kills: Int by pdc(plugin, "kills", default = 0)
player.kills++      // persisted
```

### item

Custom items with their appearance, behavior and recipe in one place.
[Detailed documentation](item/README.md)

```kotlin
items.register("ruby_sword", Material.DIAMOND_SWORD) {
  appearance { displayName = Component.text("Ruby Sword") }
  onAttack { it.damage *= 1.5 }
}
```

### storage

Per-player data loaded before join and saved asynchronously, with autosave and crash-safe writes.
[Detailed documentation](storage/README.md)

```kotlin
val stats = plugin.playerData<Stats>()
stats[player].coins += 10
```

### util

General utility module providing version checking, safe casting, and common helpers. (Coroutine support lives in `scheduler`.)
[Detailed documentation](util/README.md)

```kotlin
checkVersion("1.21.1") // throws UnsupportedVersionException if older
val player = sender.asNullable<Player>()
```

### core

Umbrella artifact to use all modules at once.
[Detailed documentation](core/README.md)

### bom

Bill of Materials to align versions across all modules.
[Detailed documentation](bom/README.md)

## License

Apache License 2.0. See [`LICENSE`](LICENSE).
