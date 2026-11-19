# DevCore Core

[English] | [[日本語](README.ja.md)]

The "umbrella" artifact that bundles the most commonly used DevCore modules.

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:core:<version>")
}
```

## Included

`adventure`, `command`, `config`, `scheduler`, `task-sequence`, `event`, `cooldown`, `gui`, `scoreboard-lite`, `effect`

## Not included

These are separate artifacts. Add only the ones you need; the BOM keeps their versions aligned.

| Module | Why you might add it |
|---|---|
| `packet` | Fake entities / blocks via PacketEvents |
| `entity` | Entity spawning, AI goals, lifecycle hooks |
| `world` | Block / chunk / player editing DSL |
| `database` | Multi-backend database access and migrations |
| `i18n` | Localized messages |
| `pdc` | Typed PersistentDataContainer access |
| `item` | Custom items with behavior and recipes |
| `storage` | Async per-player data persistence |
| `util` | Casting, version checks, small helpers |
| `scoreboard-api`, `scoreboard-nms` | The full scoreboard API and NMS implementation |

```kotlin
dependencies {
  implementation(platform("com.peco2282.devcore:devcore-bom:<version>"))
  implementation("com.peco2282.devcore:core:<version>")
  implementation("com.peco2282.devcore:i18n")
}
```
