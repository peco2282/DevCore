# DevCore World

[English] | [[日本語](README.ja.md)]

Type-safe Kotlin DSLs for editing Bukkit worlds, chunks, blocks, and players.

## Install

```kotlin
dependencies { implementation("com.peco2282.devcore:world:<version>") }
```

## Example

```kotlin
world.edit {
  time = 6_000
  weather = WeatherType.CLEAR
  block(0, 64, 0) { material = Material.STONE }
}
```
